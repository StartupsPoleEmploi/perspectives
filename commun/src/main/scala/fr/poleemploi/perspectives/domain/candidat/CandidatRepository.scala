package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.eventsourcing.AggregateRepository
import fr.poleemploi.eventsourcing.eventstore.EventStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatRepository(override val eventStore: EventStore)
  extends AggregateRepository[Candidat] {

  def newCandidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  override def getById(candidatId: CandidatId): Future[Candidat] = {
    eventStore.loadEventStream(candidatId).map(eventStream =>
      new Candidat(
        id = candidatId,
        version = eventStream.version,
        events = eventStream.events
      ))
  }
}
