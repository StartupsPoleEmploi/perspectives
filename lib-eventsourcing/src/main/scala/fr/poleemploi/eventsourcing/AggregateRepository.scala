package fr.poleemploi.eventsourcing

import fr.poleemploi.eventsourcing.eventstore.EventStore

import scala.concurrent.Future

trait AggregateRepository[A <: Aggregate] {

  def eventStore: EventStore

  def getById(aggregateId: AggregateId): Future[A]

  def save(aggregate: A, changes: List[Event]): Future[Unit] = {
    eventStore.append(
      aggregateId = aggregate.id,
      expectedVersion = aggregate.version,
      events = changes
    )
  }
}
