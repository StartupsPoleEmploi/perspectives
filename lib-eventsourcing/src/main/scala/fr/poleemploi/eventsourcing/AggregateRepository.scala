package fr.poleemploi.eventsourcing

import fr.poleemploi.eventsourcing.eventstore.{EventStore, EventStream}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait AggregateRepository[A <: Aggregate] {

  def newId: A#Id

  def eventStore: EventStore

  def snapshotRepository: SnapshotRepository[A]

  def createFromStream(aggregateId: A#Id, eventStream: EventStream): A

  def replayEvents(aggregate: A, events: List[Event]): A

  def getById(aggregateId: A#Id): Future[A] =
    snapshotRepository.findById(aggregateId).flatMap(optAggregate =>
      optAggregate.map(a =>
        eventStore
          .loadEventStreamAfterVersion(aggregateId, a.version)
          .map(eventStream => replayEvents(a, eventStream.events))
      ).getOrElse(
        eventStore
          .loadEventStream(aggregateId)
          .map(eventStream => createFromStream(aggregateId, eventStream))
      )
    )

  def save(aggregate: A, changes: List[Event]): Future[Unit] =
    eventStore.append(
      aggregateId = aggregate.id,
      expectedVersion = aggregate.version,
      events = changes
    )
}
