package fr.poleemploi.perspectives.candidat

import java.util.UUID

import fr.poleemploi.eventsourcing.eventstore.{EventStore, EventStream}
import fr.poleemploi.eventsourcing.{AggregateRepository, Event, SnapshotRepository}

class CandidatRepository(override val eventStore: EventStore,
                         override val snapshotRepository: SnapshotRepository[Candidat])
  extends AggregateRepository[Candidat] {

  def newCandidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  override def createFromStream(candidatId: CandidatId, eventStream: EventStream): Candidat =
    new Candidat(
      id = candidatId,
      version = eventStream.version,
      events = eventStream.events
    )

  override def replayEvents(aggregate: Candidat, events: List[Event]): Candidat = null
}
