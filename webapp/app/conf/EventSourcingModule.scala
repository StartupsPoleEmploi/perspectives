package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.{CandidatCommandHandler, CandidatRepository}
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService
import fr.poleemploi.perspectives.recruteur.{RecruteurCommandHandler, RecruteurRepository}
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
  def recruteurCommandHandler(recruteurRepository: RecruteurRepository,
                              commentaireService: CommentaireService): RecruteurCommandHandler =
    new RecruteurCommandHandler(
      recruteurRepository = recruteurRepository,
      commentaireService = commentaireService
    )
}
