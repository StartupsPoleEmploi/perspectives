package controllers.conseiller

import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.play.http.HttpCommandHandler
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourConseillerQuery, KeysetCandidatsPourConseiller}
import fr.poleemploi.perspectives.projections.conseiller.ConseillerQueryHandler
import fr.poleemploi.perspectives.projections.conseiller.mrs.CodeROMEsAvecHabiletesQuery
import fr.poleemploi.perspectives.projections.recruteur.{KeysetRecruteursPourConseiller, RecruteurQueryHandler, RecruteursPourConseillerQuery}
import fr.poleemploi.perspectives.recruteur.RecruteurId
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}

class ConseillerController @Inject()(cc: ControllerComponents,
                                     implicit val assets: AssetsFinder,
                                     implicit val webAppConfig: WebAppConfig,
                                     conseillerAdminAuthentifieAction: ConseillerAdminAuthentifieAction,
                                     messagesAction: MessagesActionBuilder,
                                     candidatQueryHandler: CandidatQueryHandler,
                                     conseillerQueryHandler: ConseillerQueryHandler,
                                     candidatCommandHandler: HttpCommandHandler[Candidat],
                                     recruteurQueryHandler: RecruteurQueryHandler)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def listeCandidats: Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    val query = CandidatsPourConseillerQuery(
      nbPagesACharger = 4,
      page = None
    )
    for {
      codeROMEs <- conseillerQueryHandler.handle(CodeROMEsAvecHabiletesQuery)
      candidatsPourConseillerQueryResult <- candidatQueryHandler.handle(query)
    } yield {
      Ok(views.html.conseiller.listeCandidats(
        conseillerAuthentifie = conseillerRequest.conseillerAuthentifie,
        jsData = Json.obj(
          "csrfToken" -> CSRF.getToken.map(_.value),
          "nbCandidatsParPage" -> query.nbCandidatsParPage,
          "candidats" -> candidatsPourConseillerQueryResult.candidats,
          "pages" -> candidatsPourConseillerQueryResult.pages,
          "codeROMEs" -> codeROMEs
        )
      ))
    }
  }

  def paginerCandidats: Action[AnyContent] = conseillerAdminAuthentifieAction.async { conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      PaginationCandidatForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        paginationCandidatForm =>
          candidatQueryHandler.handle(
            CandidatsPourConseillerQuery(
              nbPagesACharger = 1,
              page = Some(KeysetCandidatsPourConseiller(
                dateInscription = paginationCandidatForm.dateInscription,
                candidatId = CandidatId(paginationCandidatForm.candidatId)
              ))
            )
          ).map(result => Ok(Json.obj(
            "candidats" -> result.candidats,
            "pageSuivante" -> result.pageSuivante
          )))
      )
    }(conseillerRequest)
  }

  def ajouterMRSDHAECandidat: Action[AnyContent] = conseillerAdminAuthentifieAction.async { conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      AjouterMRSDHAECandidatForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        form => {
          val command = AjouterMRSValideesCommand(
            id = CandidatId(form.candidatId),
            mrsValidees = List(MRSValidee(
              codeROME = CodeROME(form.codeROME),
              codeDepartement = CodeDepartement(form.codeDepartement),
              dateEvaluation = form.dateEvaluation,
              isDHAE = true
            ))
          )
          candidatCommandHandler.handle(command)
        }
      )
    }(conseillerRequest)
  }

  def listeRecruteurs: Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    val query = RecruteursPourConseillerQuery(
      nbPagesACharger = 4,
      page = None
    )
    recruteurQueryHandler.handle(query).map(result =>
      Ok(views.html.conseiller.listeRecruteurs(
        conseillerAuthentifie = conseillerRequest.conseillerAuthentifie,
        jsData = Json.obj(
          "csrfToken" -> CSRF.getToken.map(_.value),
          "nbRecruteursParPage" -> query.nbRecruteursParPage,
          "recruteurs" -> result.recruteurs,
          "pages" -> result.pages,
        )
      ))
    )
  }

  def paginerRecruteurs: Action[AnyContent] = conseillerAdminAuthentifieAction.async { conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      PaginationRecruteurForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        paginationRecruteurForm => {
          recruteurQueryHandler.handle(
            RecruteursPourConseillerQuery(
              nbPagesACharger = 1,
              page = Some(KeysetRecruteursPourConseiller(
                dateInscription = paginationRecruteurForm.dateInscription,
                recruteurId = RecruteurId(paginationRecruteurForm.recruteurId)
              ))
            )
          ).map(result => Ok(Json.obj(
            "recruteurs" -> result.recruteurs,
            "pageSuivante" -> result.pageSuivante
          )))
        }
      )
    }(conseillerRequest)
  }

}
