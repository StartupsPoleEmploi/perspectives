package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.{AggregateId, AggregateRepository}
import fr.poleemploi.eventsourcing.eventstore.EventStore

class CandidatRepository(override val eventStore: EventStore)
  extends AggregateRepository[Candidat] {

  override def getById(aggregateId: AggregateId): Candidat = {
    val eventStream = eventStore.loadEventStream(aggregateId)
    new Candidat(
      id = aggregateId,
      version = eventStream.version,
      events = eventStream.events
    )
  }
}
