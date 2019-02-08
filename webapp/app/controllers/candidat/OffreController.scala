package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.commun.domain.RayonRecherche
import fr.poleemploi.perspectives.offre.domain.CriteresRechercheOffre
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import fr.poleemploi.perspectives.projections.candidat.OffresCandidatQueryResult._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsArray, Json}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class OffreController @Inject()(cc: ControllerComponents,
                                implicit val assets: AssetsFinder,
                                implicit val webAppConfig: WebAppConfig,
                                messagesAction: MessagesActionBuilder,
                                optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                candidatAuthentifieAction: CandidatAuthentifieAction,
                                candidatQueryHandler: CandidatQueryHandler,
                                rechercheCandidatQueryHandler: RechercheCandidatQueryHandler) extends AbstractController(cc) {

  def index: Action[AnyContent] = optionalCandidatAuthentifieAction.async { implicit optCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.handle(OffresCandidatQuery(CriteresRechercheOffre(
      codesROME = Nil,
      codePostal = "85000",
      rayonRecherche = RayonRecherche.MAX_10
    ))).map(offresCandidatQueryResult =>
      Ok(views.html.candidat.rechercheOffres(
        candidatAuthentifie = optCandidatAuthentifieRequest.candidatAuthentifie,
        jsData = Json.obj(
          "offres" -> offresCandidatQueryResult.offres
        )
      ))
    )
  }

  def rechercherOffres: Action[AnyContent] = optionalCandidatAuthentifieAction.async { request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      RechercheOffresForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        rechercheOffresForm => {
          candidatQueryHandler.handle(OffresCandidatQuery(buildCriteresRechercheOffre(rechercheOffresForm)))
            .map(offresCandidatQueryResult =>
              Ok(Json.obj(
                "offres" -> offresCandidatQueryResult.offres
              ))
            )
        }
      )
    }(request)
  }

  private def buildCriteresRechercheOffre(rechercheOffresForm: RechercheOffresForm): CriteresRechercheOffre =
    CriteresRechercheOffre(
      codesROME = Nil,
      codePostal = "85000",
      rayonRecherche = RayonRecherche.MAX_10
    )

}
