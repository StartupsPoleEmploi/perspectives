package fr.poleemploi.eventsourcing.infra.postgresql

import com.github.tminglei.slickpg.JsonString
import fr.poleemploi.eventsourcing.snapshotstore.{Snapshot, SnapshotStore}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Implémentation PostgreSql de SnapshotStore. <br />
  * La colonne snapshot_data est du JsonB ce qui permet de valider le JSON à l'insertion et de requêter la colonne avec les opérateurs JSON de PostgreSql.
  */
class PostgreSQLSnapshotStore(val driver: PostgresDriver,
                              database: Database) extends SnapshotStore {

  import driver.api._

  class SnapshotRecordTable(tag: Tag) extends Table[SnapshotRecordPostgreSql](tag, "snapshots") {

    def streamName = column[String]("stream_name", O.PrimaryKey)

    def streamVersion = column[Int]("stream_version")

    def streamType = column[String]("stream_type")

    def data = column[JsonString]("snapshot_data")

    def * = (streamName, streamVersion, streamType, data) <> (SnapshotRecordPostgreSql.tupled, SnapshotRecordPostgreSql.unapply)
  }

  val snapshotsTable = TableQuery[SnapshotRecordTable]

  val readRecordsQuery = Compiled { (streamName: Rep[String], streamType: Rep[String]) =>
    snapshotsTable.filter(e => e.streamName === streamName && e.streamType === streamType)
  }
  val updateSnapshotQuery = Compiled { streamName: Rep[String] =>
    for {
      s <- snapshotsTable if s.streamName === streamName
    } yield (s.streamVersion, s.data)
  }

  override def find(streamName: String, streamType: String): Future[Option[Snapshot]] =
    database
      .run(readRecordsQuery(streamName, streamType).result.headOption)
      .map(_.map(s => Snapshot(
        streamName = s.streamName,
        streamVersion = s.streamVersion,
        streamType = s.streamType,
        serializedState = s.data.value.getBytes
      )))

  override def save(snapshot: Snapshot): Future[Unit] =
    database.run(
      snapshotsTable.map(s => (s.streamName, s.streamVersion, s.data)) += (snapshot.streamName, snapshot.streamVersion, JsonString(new String(snapshot.serializedState)))
    ).map(_ => ())

  override def update(snapshot: Snapshot): Future[Unit] =
    database.run(updateSnapshotQuery(snapshot.streamName).update((
      snapshot.streamVersion,
      JsonString(new String(snapshot.serializedState))
    ))).map(_ => ())

  override def findStreamsToSnapshot(streamType: String, gap: Int): Future[List[String]] =
    database.run(
      sql"""SELECT MAX(e.stream_version), e.stream_name
            FROM events e
            LEFT JOIN snapshots fs ON fs.stream_name = e.stream_name AND fs.stream_type = $streamType
            WHERE (e.stream_version - fs.stream_version) > $gap
            OR (fs.stream_version IS NULL AND e.stream_version > $gap)
            GROUP BY e.stream_name""".as[String]
    ).map(_.toList)

}

private[postgresql] case class SnapshotRecordPostgreSql(streamName: String,
                                                        streamVersion: Int,
                                                        streamType: String,
                                                        data: JsonString)
