package fr.poleemploi.perspectives.candidat

import java.util.UUID

import fr.poleemploi.eventsourcing.eventstore.{EventStore, EventStream}
import fr.poleemploi.eventsourcing.{AggregateRepository, SnapshotRepository}

class CandidatRepository(override val eventStore: EventStore,
                         override val snapshotRepository: SnapshotRepository[Candidat])
  extends AggregateRepository[Candidat] {

  override def newId: CandidatId = CandidatId(UUID.randomUUID().toString)

  override def createFromStream(candidatId: CandidatId, eventStream: EventStream): Candidat =
    Candidat(
      id = candidatId,
      version = eventStream.version,
      state = CandidatContext().apply(eventStream.events)
    )

  override def replayEvents(candidat: Candidat, eventStream: EventStream): Candidat =
    candidat.copy(
      state = candidat.state.apply(eventStream.events)
    )
}
