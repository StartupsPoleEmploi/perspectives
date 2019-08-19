package controllers.conseiller

import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.commun.infra.play.http.HttpCommandHandler
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourConseillerQuery, KeysetCandidatsPourConseiller}
import fr.poleemploi.perspectives.projections.conseiller.ConseillerQueryHandler
import fr.poleemploi.perspectives.projections.geo.{DepartementsQuery, RegionQueryHandler, RegionsQuery}
import fr.poleemploi.perspectives.projections.metier.{MetierQueryHandler, SecteursActiviteQuery}
import fr.poleemploi.perspectives.projections.recruteur.{KeysetRecruteursPourConseiller, RecruteurQueryHandler, RecruteursPourConseillerQuery}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
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
                                     recruteurQueryHandler: RecruteurQueryHandler,
                                     regionQueryHandler: RegionQueryHandler,
                                     metiersQueryHandler: MetierQueryHandler)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  def admin: Action[AnyContent] = conseillerAdminAuthentifieAction.async { implicit conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    for {
      regions <- regionQueryHandler.handle(RegionsQuery)
      departements <- regionQueryHandler.handle(DepartementsQuery)
      secteursActivitesQueryResult <- metiersQueryHandler.handle(SecteursActiviteQuery)
    } yield
      Ok(views.html.conseiller.admin(
        conseillerAuthentifie = conseillerRequest.conseillerAuthentifie,
        jsData = Json.obj(
          "csrfToken" -> CSRF.getToken.map(_.value),
          "regions" -> regions.result,
          "departements" -> departements.result,
          "secteursActivites" -> secteursActivitesQueryResult.secteursActivites
        )
      ))
  }

  def rechercherCandidats: Action[AnyContent] = conseillerAdminAuthentifieAction.async { conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      RechercheCandidatsForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        form =>
          candidatQueryHandler.handle(
            CandidatsPourConseillerQuery(
              codesDepartement = form.codesDepartement.map(CodeDepartement),
              codePostal = form.codePostal,
              dateDebut = form.dateDebut,
              dateFin = form.dateFin,
              codeSecteurActivite = form.codeSecteurActivite.map(CodeSecteurActivite),
              page =
                for {
                  dateInscription <- form.pagination.map(_.dateInscription)
                  candidatId <- form.pagination.map(p => CandidatId(p.candidatId))
                } yield KeysetCandidatsPourConseiller(
                  dateInscription = dateInscription,
                  candidatId = candidatId
                )
            )
          ).map(result => Ok(Json.toJson(result)))
      )
    }(conseillerRequest)
  }

  def rechercherRecruteurs: Action[AnyContent] = conseillerAdminAuthentifieAction.async { conseillerRequest: ConseillerAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      RechercheRecruteursForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        form =>
          recruteurQueryHandler.handle(
            RecruteursPourConseillerQuery(
              codesDepartement = form.codesDepartement.map(CodeDepartement),
              codePostal = form.codePostal,
              dateDebut = form.dateDebut,
              dateFin = form.dateFin,
              typeRecruteur = form.typeRecruteur.flatMap(TypeRecruteur.from),
              contactParCandidats = form.contactParCandidats,
              page =
                for {
                  dateInscription <- form.pagination.map(_.dateInscription)
                  recruteurId <- form.pagination.map(p => RecruteurId(p.recruteurId))
                } yield KeysetRecruteursPourConseiller(
                  dateInscription = dateInscription,
                  recruteurId = recruteurId
                )
            )
          ).map(result => Ok(Json.toJson(result)))
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
}