package fr.poleemploi.eventsourcing

import fr.poleemploi.eventsourcing.eventstore.EventStore

trait AggregateRepository[A <: Aggregate] {

  def eventStore: EventStore

  def getById(aggregateId: AggregateId): A

  def save(aggregate: A, changes: List[Event]): Unit = {
    eventStore.append(
      aggregateId = aggregate.id,
      expectedVersion = aggregate.version,
      events = changes
    )
  }
}
