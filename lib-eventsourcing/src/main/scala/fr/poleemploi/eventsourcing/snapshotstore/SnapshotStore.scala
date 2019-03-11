package fr.poleemploi.eventsourcing.snapshotstore

import scala.concurrent.Future

trait SnapshotStore {

  def find(streamName: String, streamType: String): Future[Option[Snapshot]]

  def save(snapshot: Snapshot): Future[Unit]

  def update(snapshot: Snapshot): Future[Unit]

  def findStreamsToSnapshot(streamType: String, gap: Int): Future[List[String]]

}
