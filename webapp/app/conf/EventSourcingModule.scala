package conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore
import fr.poleemploi.eventsourcing.{AggregateRepository, Event}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.commun.infra.play.http.HttpCommandHandler
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
  def candidatSnapshotRepository(snapshotStore: SnapshotStore,
                                 @Named("eventSourcingObjectMapper")
                                 objectMapper: ObjectMapper): CandidatSnapshotRepository =
    new CandidatSnapshotRepository(
      snapshotStore = snapshotStore,
      objectMapper = objectMapper
    )

  @Provides
  @Singleton
  def candidatRepository(eventStore: EventStore,
                         snapshotRepository: CandidatSnapshotRepository): CandidatRepository =
    new CandidatRepository(
      eventStore = eventStore,
      snapshotRepository = snapshotRepository
    )

  @Provides
  @Singleton
  def candidatCommandHandler(candidatRepository: CandidatRepository,
                             cvService: CVService,
                             referentielHabiletesMRS: ReferentielHabiletesMRS,
                             localisationService: LocalisationService): CandidatCommandHandler =
    new CandidatCommandHandler {
      override val repository: AggregateRepository[Candidat] = candidatRepository

      override def configure: PartialFunction[Command[Candidat], Candidat => Future[List[Event]]] = {
        case command: InscrireCandidatCommand => c => c.inscrire(command, localisationService)
        case command: ModifierCriteresRechercheCommand => c => Future(c.modifierCriteres(command))
        case command: ConnecterCandidatCommand => c => c.connecter(command, localisationService)
        case command: AjouterCVCommand => c => c.ajouterCV(command, cvService)
        case command: RemplacerCVCommand => c => c.remplacerCV(command, cvService)
        case command: AjouterMRSValideesCommand => c => c.ajouterMRSValidee(command, referentielHabiletesMRS)
        case command: DeclarerRepriseEmploiParConseillerCommand => c => Future(c.declarerRepriseEmploiParConseiller(command))
      }
    }

  @Provides
  @Singleton
  def httpCandidatCommandHandler(commandHandler: CandidatCommandHandler): HttpCommandHandler[Candidat] =
    new HttpCommandHandler[Candidat](commandHandler)

  @Provides
  @Singleton
  def recruteurSnapshotRepository(snapshotStore: SnapshotStore,
                                  @Named("eventSourcingObjectMapper")
                                  objectMapper: ObjectMapper): RecruteurSnapshotRepository =
    new RecruteurSnapshotRepository(
      snapshotStore = snapshotStore,
      objectMapper = objectMapper
    )

  @Provides
  @Singleton
  def recruteurRepository(eventStore: EventStore,
                          snapshotRepository: RecruteurSnapshotRepository): RecruteurRepository =
    new RecruteurRepository(
      eventStore = eventStore,
      snapshotRepository = snapshotRepository
    )

  @Provides
  @Singleton
  def recruteurCommandHandler(recruteurRepository: RecruteurRepository,
                              commentaireService: CommentaireService): RecruteurCommandHandler =
    new RecruteurCommandHandler {
      override val repository: AggregateRepository[Recruteur] = recruteurRepository

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
