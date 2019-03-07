package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages.FlashMessage
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, Metier, RayonRecherche}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, TypeContrat}
import fr.poleemploi.perspectives.projections.candidat._
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsArray, Json}
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
    for {
      candidatQueryResult <- optCandidatAuthentifieRequest.candidatAuthentifie.map(c =>
        candidatQueryHandler.handle(CandidatPourRechercheOffreQuery(c.candidatId)).map(Some(_))
      ).getOrElse(Future.successful(None))
      critereLieuTravail = lieuTravail.orElse(candidatQueryResult.flatMap(_.commune))
      critereCodePostal = codePostal.orElse(candidatQueryResult.flatMap(_.codePostal))
      critereRayonRecherche = rayonRecherche.flatMap(RayonRecherche.from)
        .orElse(optCandidatAuthentifieRequest.flash.rayonRechercheModifie)
        .orElse(candidatQueryResult.flatMap(_.rayonRecherche))
      offresCandidatQueryResult <- candidatQueryHandler.handle(OffresCandidatQuery(CriteresRechercheOffre(
        motCle = motCle,
        codePostal = critereCodePostal,
        rayonRecherche = critereRayonRecherche,
        typesContrats = Nil,
        secteursActivites = Nil,
        metiers = Nil
      )))
    } yield {
      Ok(views.html.candidat.rechercheOffres(
        candidatAuthentifie = optCandidatAuthentifieRequest.candidatAuthentifie,
        jsData = Json.obj(
          "candidatAuthentifie" -> optCandidatAuthentifieRequest.isCandidatAuthentifie,
          "cv" -> candidatQueryResult.exists(_.cv),
          "offres" -> offresCandidatQueryResult.offres,
          "nbOffresTotal" -> offresCandidatQueryResult.nbOffresTotal,
          "csrfToken" -> CSRF.getToken.map(_.value),
          "recherche" -> Json.obj(
            "motCle" -> motCle,
            "lieuTravail" -> critereLieuTravail,
            "codePostal" -> critereCodePostal,
            "rayonRecherche" -> critereRayonRecherche.map(_.value),
            "metiersEvalues" -> JsArray(candidatQueryResult.map(_.metiersEvalues).getOrElse(List[Metier]()).map(m =>
              Json.obj(
                "label" -> m.label,
                "value" -> m.codeROME
              )
            ))
          ),
          "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
        )
      ))
    }
  }

  def rechercherOffres: Action[AnyContent] = optionalCandidatAuthentifieAction.async { request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def buildCriteresRechercheOffre(rechercheOffresForm: RechercheOffresForm): CriteresRechercheOffre =
        CriteresRechercheOffre(
          motCle = rechercheOffresForm.motCle,
          codePostal = rechercheOffresForm.codePostal,
          rayonRecherche = rechercheOffresForm.rayonRecherche.flatMap(RayonRecherche.from),
          typesContrats = rechercheOffresForm.typesContrats.flatMap(TypeContrat.from),
          secteursActivites = rechercheOffresForm.secteursActivites.flatMap(CodeSecteurActivite.from),
          metiers = rechercheOffresForm.metiers.map(CodeROME)
        )

      RechercheOffresForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        rechercheOffresForm =>
          candidatQueryHandler.handle(OffresCandidatQuery(buildCriteresRechercheOffre(rechercheOffresForm)))
            .map(offresCandidatQueryResult =>
              Ok(Json.obj(
                "offres" -> offresCandidatQueryResult.offres,
                "nbOffresTotal" -> offresCandidatQueryResult.nbOffresTotal
              ))
            )
      )
    }(request)
  }
}
