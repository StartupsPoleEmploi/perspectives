package fr.poleemploi.perspectives.domain.demandeurEmploi

import fr.poleemploi.eventsourcing.{AggregateId, AggregateRepository}
import fr.poleemploi.eventsourcing.eventstore.EventStore

class DemandeurEmploiRepository(override val eventStore: EventStore)
  extends AggregateRepository[DemandeurEmploi] {

  override def getById(aggregateId: AggregateId): DemandeurEmploi = {
    val eventStream = eventStore.loadEventStream(aggregateId)
    new DemandeurEmploi(
      id = aggregateId,
      version = eventStream.version,
      events = eventStream.events
    )
  }
}
