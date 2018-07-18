package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.AggregateRepository
import fr.poleemploi.eventsourcing.eventstore.EventStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatRepository(override val eventStore: EventStore)
  extends AggregateRepository[Candidat] {

  override def getById(candidatId: CandidatId): Future[Candidat] = {
    eventStore.loadEventStream(candidatId).map(eventStream =>
      new Candidat(
        id = candidatId,
        version = eventStream.version,
        events = eventStream.events
      ))
  }
}
