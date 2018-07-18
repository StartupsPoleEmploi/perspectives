package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.AggregateRepository
import fr.poleemploi.eventsourcing.eventstore.EventStore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurRepository(override val eventStore: EventStore)
  extends AggregateRepository[Recruteur] {

  override def getById(recruteurId: RecruteurId): Future[Recruteur] = {
    eventStore.loadEventStream(recruteurId).map(eventStream =>
      new Recruteur(
        id = recruteurId,
        version = eventStream.version,
        events = eventStream.events
      ))
  }
}
