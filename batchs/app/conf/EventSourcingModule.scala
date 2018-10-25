package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.{CandidatCommandHandler, CandidatRepository}
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
  def candidatCommandHandler(candidatRepository: CandidatRepository,
                             cvService: CVService): CandidatCommandHandler =
    new CandidatCommandHandler(
      candidatRepository = candidatRepository,
      cvService = cvService
    )
}
