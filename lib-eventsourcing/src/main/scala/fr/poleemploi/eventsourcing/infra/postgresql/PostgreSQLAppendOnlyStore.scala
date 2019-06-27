package fr.poleemploi.eventsourcing.infra.postgresql

import java.sql.SQLException

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tminglei.slickpg.JsonString
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyData, AppendOnlyStore, AppendOnlyStoreConcurrencyException, AppendedEvent}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Implémentation PostgreSql de AppendOnlyStore. <br />
  * La colonne event_data est du JsonB ce qui permet de valider le JSON à l'insertion et de requêter la colonne avec les opérateurs JSON de PostgreSql.
  */
class PostgreSQLAppendOnlyStore(val driver: PostgresDriver,
                                database: Database,
                                objectMapper: ObjectMapper) extends AppendOnlyStore {

  import driver.api._

  class EventRecordTable(tag: Tag) extends Table[EventRecordPostgreSql](tag, "events") {

    def id = column[Long]("id", O.PrimaryKey)

    def streamName = column[String]("stream_name")

    def streamVersion = column[Int]("stream_version")

    def data = column[JsonString]("event_data")

    def eventType = column[String]("event_type")

    def * = (id, streamName, streamVersion, data, eventType) <> (EventRecordPostgreSql.tupled, EventRecordPostgreSql.unapply)
  }

  val eventsTable = TableQuery[EventRecordTable]

  val uniqueConstraintViolationCode = "23505"

  val readRecordsQuery = Compiled { streamName: Rep[String] =>
    eventsTable
      .filter(e => e.streamName === streamName)
      .sortBy(_.streamVersion.asc)
  }

  val readRecordsAfterVersionQuery = Compiled { (streamName: Rep[String], version: Rep[Int]) =>
    eventsTable
      .filter(e => e.streamName === streamName && e.streamVersion > version)
      .sortBy(_.streamVersion.asc)
  }

  val lastStreamVersionQuery = Compiled { streamName: Rep[String] =>
    eventsTable
      .filter(e => e.streamName === streamName)
      .map(_.streamVersion).max
  }

  override def append(streamName: String,
                      expectedStreamVersion: Int,
                      datas: List[AppendOnlyData]): Future[Unit] = {
    val actions = datas.map(
      d => eventsTable.map(e => (e.streamName, e.streamVersion, e.eventType, e.data))
        += (streamName, d.streamVersion, d.eventType, serializeEvent(d.event))
    )

    database.run(DBIO.sequence(actions))
      .map(_ => ())
      .recoverWith {
        case e: SQLException if e.getSQLState == uniqueConstraintViolationCode =>
          getLastStreamVersion(streamName).flatMap(lastStreamVersion =>
            Future.failed(AppendOnlyStoreConcurrencyException(
              expectedStreamVersion = expectedStreamVersion,
              actualStreamVersion = lastStreamVersion,
              streamName = streamName
            ))
          )
      }
  }

  override def readRecords(streamName: String, version: Option[Int]): Future[List[AppendedEvent]] =
    database.run(version
      .map(v => readRecordsAfterVersionQuery(streamName, v))
      .getOrElse(readRecordsQuery(streamName)).result)
      .map(_.toList.map(eventRecord =>
        AppendedEvent(
          streamName = eventRecord.streamName,
          streamVersion = eventRecord.streamVersion,
          event = unserializeEvent(eventRecord.data)
        )
      ))

  def streamRecords: Source[AppendedEvent, NotUsed] =
    Source.fromPublisher {
      database.stream(
        eventsTable
          .sortBy(e => (e.streamName, e.streamVersion))
          .result
          .transactionally
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 1000
          )
      ).mapResult(eventRecord =>
        AppendedEvent(
          streamName = eventRecord.streamName,
          streamVersion = eventRecord.streamVersion,
          event = unserializeEvent(eventRecord.data)
        )
      )
    }

  private def getLastStreamVersion(streamName: String): Future[Int] = database.run(lastStreamVersionQuery(streamName).result).map(_.getOrElse(0))

  private def serializeEvent(event: Event): JsonString = JsonString(Event.toJson(event)(objectMapper))

  private def unserializeEvent(data: JsonString): Event = Event.fromJson(data.value)(objectMapper)
}

private[postgresql] case class EventRecordPostgreSql(id: Long,
                                                     streamName: String,
                                                     streamVersion: Int,
                                                     data: JsonString,
                                                     eventType: String)