package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.perspectives.domain.candidat.cv.CVService
import fr.poleemploi.perspectives.domain.candidat.{CandidatCommandHandler, CandidatRepository}
import fr.poleemploi.perspectives.domain.recruteur.{RecruteurCommandHandler, RecruteurRepository}
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

  @Provides
  @Singleton
  def recruteurRepository(eventStore: EventStore): RecruteurRepository =
    new RecruteurRepository(
      eventStore = eventStore
    )

  @Provides
  @Singleton
  def recruteurCommandHandler(recruteurRepository: RecruteurRepository): RecruteurCommandHandler =
    new RecruteurCommandHandler(
      recruteurRepository = recruteurRepository
    )
}
