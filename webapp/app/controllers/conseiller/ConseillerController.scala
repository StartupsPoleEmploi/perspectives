package controllers.conseiller

import java.time.ZonedDateTime

import authentification.infra.play.{ConseillerAdminAuthentifieAction, ConseillerAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat.{CandidatCommandHandler, CandidatId, DeclarerRepriseEmploiParConseillerCommand}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourConseillerQuery}
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurQueryHandler, RecruteursPourConseillerQuery}
import javax.inject.Inject
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

class ConseillerController @Inject()(cc: ControllerComponents,
                                     implicit val assets: AssetsFinder,
                                     implicit val webAppConfig: WebAppConfig,
                                     conseillerAdminAuthentifieAction: ConseillerAdminAuthentifieAction,
                                     messagesAction: MessagesActionBuilder,
                                     candidatQueryHandler: CandidatQueryHandler,
                                     candidatCommandHandler: CandidatCommandHandler,
                                     recruteurQueryHandler: RecruteurQueryHandler) extends AbstractController(cc) {

  val nbCandidatsParPage = 20
  val nbRecruteursParPage = 20

  def listeCandidats: Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    val avantDateInscription = ZonedDateTime.now()
    candidatQueryHandler.listerPourConseiller(
      CandidatsPourConseillerQuery(
        nbCandidatsParPage = nbCandidatsParPage,
        nbPagesACharger = 4,
        avantDateInscription = avantDateInscription
      )
    ).map(result =>
      Ok(views.html.conseiller.listeCandidats(
        resultat = result,
        nbCandidatsParPage = nbCandidatsParPage))
    )
  }

  def paginationCandidats(avantDateInscription: String): Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.listerPourConseiller(
      CandidatsPourConseillerQuery(
        nbCandidatsParPage = nbCandidatsParPage,
        nbPagesACharger = 1,
        avantDateInscription = ZonedDateTime.parse(avantDateInscription)
      )
    ).map(result => Ok(views.html.conseiller.partials.candidats(result)))
  }

  def declarerRepriseEmploi(candidatId: String): Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    candidatCommandHandler.handle(
      DeclarerRepriseEmploiParConseillerCommand(
        id = CandidatId(candidatId),
        conseillerId = conseillerRequest.conseillerId
      )
    ).map(_ => NoContent)
  }

  def listeRecruteurs: Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    val avantDateInscription = ZonedDateTime.now()
    recruteurQueryHandler.listerPourConseiller(
      RecruteursPourConseillerQuery(
        nbRecruteursParPage = nbRecruteursParPage,
        nbPagesACharger = 4,
        avantDateInscription = avantDateInscription
      )
    ).map(result =>
      Ok(views.html.conseiller.listeRecruteurs(
        recruteurs = result.recruteurs,
        pagesInitiales = result.pages,
        nbRecruteursParPage = nbRecruteursParPage))
    )
  }

  def paginationRecruteurs(avantDateInscription: String): Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    recruteurQueryHandler.listerPourConseiller(
      RecruteursPourConseillerQuery(
        nbRecruteursParPage = nbRecruteursParPage,
        nbPagesACharger = 1,
        avantDateInscription = ZonedDateTime.parse(avantDateInscription)
      )
    ).map(result => Ok(views.html.conseiller.partials.recruteurs(result.recruteurs)))
  }

}
