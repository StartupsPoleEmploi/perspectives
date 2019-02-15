package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.commun.domain.{CodeSecteurActivite, RayonRecherche}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, TypeContrat}
import fr.poleemploi.perspectives.projections.candidat._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RechercheOffreController @Inject()(cc: ControllerComponents,
                                         implicit val assets: AssetsFinder,
                                         implicit val webAppConfig: WebAppConfig,
                                         messagesAction: MessagesActionBuilder,
                                         optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                         candidatAuthentifieAction: CandidatAuthentifieAction,
                                         candidatQueryHandler: CandidatQueryHandler) extends AbstractController(cc) {

  def index(motCle: Option[String], codePostal: Option[String], lieuTravail: Option[String], rayonRecherche: Option[Int]): Action[AnyContent] = optionalCandidatAuthentifieAction.async { implicit optCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.handle(OffresCandidatQuery(CriteresRechercheOffre(
      motCle = motCle,
      codePostal = codePostal,
      rayonRecherche = rayonRecherche.flatMap(RayonRecherche.from),
      typesContrats = Nil,
      secteursActivites = Nil
    ))).map(offresCandidatQueryResult =>
      Ok(views.html.candidat.rechercheOffres(
        candidatAuthentifie = optCandidatAuthentifieRequest.candidatAuthentifie,
        jsData = Json.obj(
          "candidatAuthentifie" -> optCandidatAuthentifieRequest.isCandidatAuthentifie,
          "offres" -> offresCandidatQueryResult.offres,
          "csrfToken" -> CSRF.getToken.map(_.value),
          "recherche" -> Json.obj(
            "motCle" -> motCle,
            "lieuTravail" -> lieuTravail,
            "codePostal" -> codePostal,
            "rayonRecherche" -> rayonRecherche
          ),
          "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
        )
      ))
    )
  }

  def rechercherOffres: Action[AnyContent] = optionalCandidatAuthentifieAction.async { request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def buildCriteresRechercheOffre(rechercheOffresForm: RechercheOffresForm): CriteresRechercheOffre =
        CriteresRechercheOffre(
          motCle = rechercheOffresForm.motCle,
          codePostal = rechercheOffresForm.codePostal,
          rayonRecherche = rechercheOffresForm.rayonRecherche.flatMap(RayonRecherche.from),
          typesContrats = rechercheOffresForm.typesContrats.flatMap(TypeContrat.from),
          secteursActivites = rechercheOffresForm.secteursActivites.flatMap(CodeSecteurActivite.from)
        )

      RechercheOffresForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        rechercheOffresForm =>
          candidatQueryHandler.handle(OffresCandidatQuery(buildCriteresRechercheOffre(rechercheOffresForm)))
            .map(offresCandidatQueryResult =>
              Ok(Json.obj(
                "offres" -> offresCandidatQueryResult.offres
              ))
            )
      )
    }(request)
  }
}
