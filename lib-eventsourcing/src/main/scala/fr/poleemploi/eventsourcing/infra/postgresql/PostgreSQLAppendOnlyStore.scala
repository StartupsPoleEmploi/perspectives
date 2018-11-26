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

  override def append(streamName: String,
                      expectedStreamVersion: Int,
                      datas: List[AppendOnlyData]): Future[Unit] = {
    // TODO : selectionner la derniere version du stream dans la meme transaction que l'insert
    val actions = datas.map(
      d => eventsTable.map(e => (e.streamName, e.streamVersion, e.eventType, e.data))
        += (streamName, d.streamVersion, d.eventType, JsonString(serializeData(d.event)))
    )

    database.run(DBIO.sequence(actions))
      .map(_ => ())
      .recoverWith {
        // TODO : uniqueConstraint n'est pas forcément une AppendOnlyStoreConcurrencyException
        case e: SQLException if e.getSQLState == uniqueConstraintViolationCode =>
          Future.failed(throw AppendOnlyStoreConcurrencyException(
            expectedStreamVersion = expectedStreamVersion,
            actualStreamVersion = getLastStreamVersion(streamName),
            streamName = streamName
          ))
      }
  }

  override def readRecords(streamName: String): Future[List[AppendedEvent]] =
    database.run(readRecordsQuery(streamName).result)
      .map(_.toList.map(eventRecord =>
        AppendedEvent(
          streamName = eventRecord.streamName,
          streamVersion = eventRecord.streamVersion,
          event = unserializeData(eventRecord.data.value)
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
          event = unserializeData(eventRecord.data.value)
        )
      )
    }

  private def getLastStreamVersion(streamName: String): Int = 0

  private def serializeData(event: Event): String = Event.toJson(event)(objectMapper)

  private def unserializeData(data: String): Event = Event.fromJson(data)(objectMapper)
}

private[postgresql] case class EventRecordPostgreSql(id: Long,
                                                     streamName: String,
                                                     streamVersion: Int,
                                                     data: JsonString,
                                                     eventType: String)