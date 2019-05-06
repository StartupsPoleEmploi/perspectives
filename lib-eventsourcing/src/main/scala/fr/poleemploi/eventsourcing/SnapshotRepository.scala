package fr.poleemploi.eventsourcing

import fr.poleemploi.eventsourcing.snapshotstore.{Snapshot, SnapshotStore}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SnapshotRepository[A <: Aggregate] {

  def snapshotStore: SnapshotStore

  def aggregateType: String

  def gapBetweenSnapshots: Int = 20

  def deserialize(id: A#Id, version: Int, state: Array[Byte]): A

  def serialize(aggregate: A): Array[Byte]

  def fromStreamName(streamName: String): A#Id

  def findById(aggregateId: A#Id): Future[Option[A]] =
    snapshotStore.find(
      streamName = aggregateId.value,
      streamType = aggregateType
    ).map(optSnapshot =>
      optSnapshot.map(s => deserialize(aggregateId, s.streamVersion, s.serializedState))
    )

  def findIdsToSnapshot: Future[List[A#Id]] =
    snapshotStore.findStreamsToSnapshot(
      streamType = aggregateType,
      gap = gapBetweenSnapshots
    ).map(_.map(fromStreamName))

  def save(aggregate: A): Future[Unit] =
    snapshotStore.save(Snapshot(
      streamName = aggregate.id.value,
      streamVersion = aggregate.version,
      streamType = aggregateType,
      serializedState = serialize(aggregate)
    ))
}
