package controllers.conseiller

import java.time.ZonedDateTime

import authentification.infra.play.{ConseillerAuthentifieAction, ConseillerAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat.{CandidatCommandHandler, CandidatId, DeclarerRepriseEmploiParConseillerCommand}
import fr.poleemploi.perspectives.conseiller.{AutorisationService, RoleConseiller}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourConseillerQuery}
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurQueryHandler, RecruteursPourConseillerQuery}
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConseillerController @Inject()(cc: ControllerComponents,
                                     implicit val assets: AssetsFinder,
                                     implicit val webAppConfig: WebAppConfig,
                                     conseillerAuthentifieAction: ConseillerAuthentifieAction,
                                     messagesAction: MessagesActionBuilder,
                                     autorisationService: AutorisationService,
                                     candidatQueryHandler: CandidatQueryHandler,
                                     candidatCommandHandler: CandidatCommandHandler,
                                     recruteurQueryHandler: RecruteurQueryHandler) extends AbstractController(cc) {

  val nbCandidatsParPage = 20
  val nbRecruteursParPage = 20

  def listeCandidats: Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      val avantDateInscription = ZonedDateTime.now()
      candidatQueryHandler.listerPourConseiller(
        CandidatsPourConseillerQuery(
          nbCandidatsParPage = nbCandidatsParPage,
          avantDateInscription = avantDateInscription
        )
      ).map(candidats =>
        Ok(views.html.conseiller.listeCandidats(
          candidats = candidats,
          avantDateInscription = avantDateInscription,
          nbCandidatsParPage = nbCandidatsParPage))
      )
    } else Future.successful(Unauthorized)
  }

  def paginationCandidats(avantDateInscription: String): Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      candidatQueryHandler.listerPourConseiller(
        CandidatsPourConseillerQuery(
          nbCandidatsParPage = nbCandidatsParPage,
          avantDateInscription = ZonedDateTime.parse(avantDateInscription)
        )
      ).map(candidats => Ok(views.html.conseiller.partials.candidats(candidats, nbCandidatsParPage)))
    }
    else Future.successful(Unauthorized)
  }

  def declarerRepriseEmploi(candidatId: String): Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      candidatCommandHandler.declarerRepriseEmploi(
        DeclarerRepriseEmploiParConseillerCommand(
          id = CandidatId(candidatId),
          conseillerId = conseillerRequest.conseillerId
        )
      ).map(_ => NoContent)
    } else Future.successful(Unauthorized)
  }

  def listeRecruteurs: Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      val avantDateInscription = ZonedDateTime.now()
      recruteurQueryHandler.listerPourConseiller(
        RecruteursPourConseillerQuery(
          nbRecruteursParPage = nbRecruteursParPage,
          avantDateInscription = avantDateInscription
        )
      ).map(recruteurs =>
        Ok(views.html.conseiller.listeRecruteurs(
          recruteurs = recruteurs,
          avantDateInscription = avantDateInscription,
          nbRecruteursParPage = nbRecruteursParPage))
      )
    } else Future.successful(Unauthorized)
  }

  def paginationRecruteurs(avantDateInscription: String): Action[AnyContent] = conseillerAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    if (autorisationService.hasRole(conseillerRequest.conseillerId, RoleConseiller.ADMIN)) {
      recruteurQueryHandler.listerPourConseiller(
        RecruteursPourConseillerQuery(
          nbRecruteursParPage = nbRecruteursParPage,
          avantDateInscription = ZonedDateTime.parse(avantDateInscription)
        )
      ).map(recruteurs => Ok(views.html.conseiller.partials.recruteurs(recruteurs, nbRecruteursParPage)))
    }
    else Future.successful(Unauthorized)
  }

}
