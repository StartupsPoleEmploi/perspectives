package fr.poleemploi.eventsourcing.infra.sql

import java.sql.SQLException

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tminglei.slickpg.JsonString
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyData, AppendOnlyStore, AppendOnlyStoreConcurrencyException}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

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

  override def append(streamName: String,
                      expectedStreamVersion: Int,
                      datas: List[AppendOnlyData]): Unit = {
    // TODO : selectionner la derniere version du stream dans la meme transaction que l'insert
    val actions = datas.map(
      d => eventsTable.map(e => (e.streamName, e.streamVersion, e.eventType, e.data))
        += (streamName, d.streamVersion, d.eventType, JsonString(serializeData(d.event)))
    )
    val f = database.run(DBIO.sequence(actions)) recoverWith {
      // TODO : uniqueConstraint n'est pas forcément une AppendOnlyStoreConcurrencyException
      case e: SQLException if e.getSQLState == uniqueConstraintViolationCode =>
        Future.failed(throw AppendOnlyStoreConcurrencyException(
          expectedStreamVersion = expectedStreamVersion,
          actualStreamVersion = getLastStreamVersion(streamName),
          streamName = streamName
        ))
    }
    Await.ready(f, 5.seconds)
  }

  override def readRecords(streamName: String): List[AppendOnlyData] = {
    val query = eventsTable
      .filter(e => e.streamName === streamName)
      .sortBy(_.streamVersion)

    Await.result(
      database.run(query.result)
        .map(_.toList.map(f => AppendOnlyData(
          streamVersion = f.streamVersion,
          eventType = f.eventType,
          event = unserializeData(f.data.value))
        ))
      , 5.seconds
    )
  }

  private def getLastStreamVersion(streamName: String): Int = 0

  private def serializeData(event: Event): String = Event.toJson(event)(objectMapper)

  private def unserializeData(data: String): Event = Event.fromJson(data)(objectMapper)
}

private[sql] case class EventRecordPostgreSql(id: Long,
                                              streamName: String,
                                              streamVersion: Int,
                                              data: JsonString,
                                              eventType: String)