package controllers.candidat

import authentification.infra.play.{CandidatAuthentifieAction, OptionalCandidatAuthentifieAction, OptionalCandidatAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages.FlashMessage
import fr.poleemploi.perspectives.candidat.LocalisationRecherche
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, RayonRecherche}
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
    def buildLocalisationRechercheFromRequest: Option[LocalisationRecherche] =
      for {
        commune <- lieuTravail
        codePostal <- codePostal
      } yield LocalisationRecherche(
        commune = commune,
        codePostal = codePostal,
        coordonnees = null,
        rayonRecherche = rayonRecherche.flatMap(RayonRecherche.from)
      )

    for {
      candidatQueryResult <- optCandidatAuthentifieRequest.candidatAuthentifie.map(c =>
        candidatQueryHandler.handle(CandidatPourRechercheOffreQuery(c.candidatId)).map(Some(_))
      ).getOrElse(Future.successful(None))
      localisationRecherche = optCandidatAuthentifieRequest.flash.candidatLocalisationRechercheModifiee
        .orElse(buildLocalisationRechercheFromRequest)
        .orElse(candidatQueryResult.flatMap(_.localisationRecherche))
      offresCandidatQueryResult <- candidatQueryHandler.handle(OffresCandidatQuery(CriteresRechercheOffre(
        motCle = motCle,
        codePostal = localisationRecherche.map(_.codePostal),
        rayonRecherche = localisationRecherche.flatMap(_.rayonRecherche),
        typesContrats = Nil,
        secteursActivites = Nil,
        codesROME = Nil
      )))
    } yield {
      Ok(views.html.candidat.rechercheOffres(
        candidatAuthentifie = optCandidatAuthentifieRequest.candidatAuthentifie,
        jsData = Json.obj(
          "candidatAuthentifie" -> optCandidatAuthentifieRequest.isCandidatAuthentifie,
          "cv" -> candidatQueryResult.exists(_.cv),
          "metiersValides" -> candidatQueryResult.map(_.metiersValides),
          "offres" -> offresCandidatQueryResult.offres,
          "nbOffresTotal" -> offresCandidatQueryResult.nbOffresTotal,
          "csrfToken" -> CSRF.getToken.map(_.value),
          "recherche" -> Json.obj(
            "motCle" -> motCle,
            "lieuTravail" -> localisationRecherche.map(_.commune),
            "codePostal" -> localisationRecherche.map(_.codePostal),
            "rayonRecherche" -> localisationRecherche
              .flatMap(_.rayonRecherche.map(_.value))
              .orElse(Some(0)) // FIXME : unite
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
          secteursActivites = rechercheOffresForm.secteursActivites.map(CodeSecteurActivite),
          codesROME = rechercheOffresForm.metiers.map(CodeROME)
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
