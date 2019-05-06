package fr.poleemploi.perspectives.recruteur

import java.util.UUID

import fr.poleemploi.eventsourcing.eventstore.{EventStore, EventStream}
import fr.poleemploi.eventsourcing.{AggregateRepository, SnapshotRepository}

class RecruteurRepository(override val eventStore: EventStore,
                          override val snapshotRepository: SnapshotRepository[Recruteur])
  extends AggregateRepository[Recruteur] {

  override def newId: RecruteurId = RecruteurId(UUID.randomUUID().toString)

  override def createFromStream(recruteurId: RecruteurId, eventStream: EventStream): Recruteur =
    Recruteur(
      id = recruteurId,
      version = eventStream.version,
      state = RecruteurContext().apply(eventStream.events)
    )

  override def replayEvents(recruteur: Recruteur, eventStream: EventStream): Recruteur =
    recruteur.copy(
      state = recruteur.state.apply(eventStream.events)
    )
}
