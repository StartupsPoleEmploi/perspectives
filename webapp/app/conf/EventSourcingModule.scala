package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, CandidatRepository}
import javax.inject.Singleton

class EventSourcingModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def candidatRepository(eventStore: EventStore): CandidatRepository =
    new CandidatRepository(
      eventStore = eventStore
    )

  @Provides
  @Singleton
  def candidatCommandHandler(candidatRepository: CandidatRepository): CandidatCommandHandler =
    new CandidatCommandHandler(
      candidatRepository = candidatRepository
    )
}
