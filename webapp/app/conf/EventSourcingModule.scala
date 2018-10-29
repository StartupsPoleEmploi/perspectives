package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.eventsourcing.{AggregateRepository, Event}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.alerte.domain.AlerteId
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService
import javax.inject.Singleton

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    new CandidatCommandHandler {
      override def repository: AggregateRepository[Candidat] = candidatRepository

      override def newCandidatId: CandidatId = candidatRepository.newCandidatId

      override def configure: PartialFunction[Command[Candidat], Candidat => Future[List[Event]]] = {
        case command: InscrireCandidatCommand => c => Future(c.inscrire(command))
        case command: ModifierCriteresRechercheCommand => c => Future(c.modifierCriteres(command))
        case command: ConnecterCandidatCommand => c => Future(c.connecter(command))
        case command: AjouterCVCommand => c => c.ajouterCV(command, cvService)
        case command: RemplacerCVCommand => c => c.remplacerCV(command, cvService)
        case command: AjouterMRSValideesCommand => c => Future(c.ajouterMRSValidee(command))
        case command: DeclarerRepriseEmploiParConseillerCommand => c => Future(c.declarerRepriseEmploiParConseiller(command))
      }
    }

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
    new RecruteurCommandHandler {
      override def repository: AggregateRepository[Recruteur] = recruteurRepository

      override def newRecruteurId: RecruteurId = recruteurRepository.newRecruteurId

      override def newAlerteId: AlerteId = recruteurRepository.newAlerteId

      override def configure: PartialFunction[Command[Recruteur], Recruteur => Future[List[Event]]] = {
        case command: InscrireRecruteurCommand => r => Future(r.inscrire(command))
        case command: ConnecterRecruteurCommand => r => Future(r.connecter(command))
        case command: ModifierProfilCommand => r => Future(r.modifierProfil(command))
        case command: CommenterListeCandidatsCommand => r => r.commenterListeCandidats(command, commentaireService)
        case command: CreerAlerteCommand => r => Future(r.creerAlerte(command))
        case command: SupprimerAlerteCommand => r => Future(r.supprimerAlerte(command))
      }
    }
}
