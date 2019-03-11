package fr.poleemploi.perspectives.recruteur

import java.util.UUID

import fr.poleemploi.eventsourcing.eventstore.{EventStore, EventStream}
import fr.poleemploi.eventsourcing.{AggregateRepository, Event, SnapshotRepository}
import fr.poleemploi.perspectives.recruteur.alerte.domain.AlerteId

class RecruteurRepository(override val eventStore: EventStore,
                          override val snapshotRepository: SnapshotRepository[Recruteur])
  extends AggregateRepository[Recruteur] {

  def newRecruteurId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  def newAlerteId: AlerteId = AlerteId(UUID.randomUUID().toString)

  override def createFromStream(recruteurId: RecruteurId, eventStream: EventStream) =
    new Recruteur(
      id = recruteurId,
      version = eventStream.version,
      events = eventStream.events
    )

  override def replayEvents(aggregate: Recruteur, events: List[Event]): Recruteur = null
}
