package controllers.conseiller

import java.time.ZonedDateTime

import authentification.infra.play.{ConseillerAdminAuthentifieAction, ConseillerAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat.{CandidatCommandHandler, CandidatId, DeclarerRepriseEmploiParConseillerCommand}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourConseillerQuery, KeysetCandidatsPourConseiller}
import fr.poleemploi.perspectives.projections.recruteur.{KeysetRecruteursPourConseiller, RecruteurQueryHandler, RecruteursPourConseillerQuery}
import fr.poleemploi.perspectives.recruteur.RecruteurId
import javax.inject.Inject
import play.api.libs.json.Json
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

  def listeCandidats: Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    val query = CandidatsPourConseillerQuery(
      nbPagesACharger = 4,
      page = None
    )
    candidatQueryHandler.handle(query).map(result =>
      Ok(views.html.conseiller.listeCandidats(
        candidats = result.candidats,
        jsData = Json.obj(
          "nbCandidatsParPage" -> query.nbCandidatsParPage,
          "pagesInitiales" -> result.pages
        )
      ))
    )
  }

  def paginationCandidats(dateInscription: Long, candidatId: String): Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.handle(
      CandidatsPourConseillerQuery(
        nbPagesACharger = 1,
        page =  Some(KeysetCandidatsPourConseiller(
          dateInscription = dateInscription,
          candidatId = CandidatId(candidatId)
        ))
      )
    ).map(result => Ok(Json.obj(
      "html" -> views.html.conseiller.partials.candidats(result.candidats).body.replaceAll("\n", ""),
      "nbCandidats" -> result.candidats.size,
      "pageSuivante" -> result.pageSuivante
    )))
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
    val query = RecruteursPourConseillerQuery(
      nbPagesACharger = 4,
      page = None
    )
    recruteurQueryHandler.handle(query).map(result =>
      Ok(views.html.conseiller.listeRecruteurs(
        recruteurs = result.recruteurs,
        jsData = Json.obj(
          "nbRecruteursParPage" -> query.nbRecruteursParPage,
          "pagesInitiales" -> result.pages
        )
      ))
    )
  }

  def paginationRecruteurs(dateInscription: String, recruteurId: String): Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    recruteurQueryHandler.handle(
      RecruteursPourConseillerQuery(
        nbPagesACharger = 1,
        page =  Some(KeysetRecruteursPourConseiller(
          dateInscription = ZonedDateTime.parse(dateInscription),
          recruteurId = RecruteurId(recruteurId)
        ))
      )
    ).map(result => Ok(Json.obj(
      "html" -> views.html.conseiller.partials.recruteurs(result.recruteurs).body.replaceAll("\n", ""),
      "nbRecruteurs" -> result.recruteurs.size,
      "pageSuivante" -> result.pageSuivante
    )))
  }

}
