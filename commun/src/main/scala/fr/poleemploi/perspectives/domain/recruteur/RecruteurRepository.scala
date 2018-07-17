package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.eventsourcing.{AggregateId, AggregateRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurRepository(override val eventStore: EventStore)
  extends AggregateRepository[Recruteur] {

  override def getById(aggregateId: AggregateId): Future[Recruteur] = {
    eventStore.loadEventStream(aggregateId).map( eventStream =>
      new Recruteur(
        id = aggregateId,
        version = eventStream.version,
        events = eventStream.events
      ))
  }
}
