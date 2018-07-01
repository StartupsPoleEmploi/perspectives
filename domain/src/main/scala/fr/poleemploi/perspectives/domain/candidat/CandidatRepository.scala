package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.{AggregateId, AggregateRepository}
import fr.poleemploi.eventsourcing.eventstore.EventStore

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CandidatRepository(override val eventStore: EventStore)
  extends AggregateRepository[Candidat] {

  override def getById(aggregateId: AggregateId): Future[Candidat] = {
    eventStore.loadEventStream(aggregateId).map( eventStream =>
    new Candidat(
      id = aggregateId,
      version = eventStream.version,
      events = eventStream.events
    ))
  }
}
