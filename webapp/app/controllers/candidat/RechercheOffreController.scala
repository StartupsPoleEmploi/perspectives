package controllers.candidat

import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import fr.poleemploi.perspectives.commun.domain.{CodeROME, UniteLongueur}
import fr.poleemploi.perspectives.metier.domain.SecteurActivite
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, PageOffres, RayonRecherche, TypeContrat}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.metier.{MetierQueryHandler, SecteursActiviteQuery}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc._
import play.filters.csrf.CSRF
import play.twirl.api.HtmlFormat
import security.QueryParamSanitizer
import tracking.TrackingService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RechercheOffreController @Inject()(cc: ControllerComponents,
                                         implicit val assets: AssetsFinder,
                                         implicit val webAppConfig: WebAppConfig,
                                         messagesAction: MessagesActionBuilder,
                                         optionalCandidatAuthentifieAction: OptionalCandidatAuthentifieAction,
                                         candidatQueryHandler: CandidatQueryHandler,
                                         metierQueryHandler: MetierQueryHandler)(implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  def index(codePostal: Option[String], lieuTravail: Option[String], rayonRecherche: Option[Int]): Action[AnyContent] = optionalCandidatAuthentifieAction.async { optCandidatAuthentifieRequest: OptionalCandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def buildLocalisationOffresFromRequest: Option[LocalisationOffresForm] =
        for {
          lieuTravail <- QueryParamSanitizer.sanitize(lieuTravail)
          codePostal <- QueryParamSanitizer.sanitize(codePostal)
        } yield LocalisationOffresForm(
          lieuTravail = lieuTravail,
          codePostal = codePostal,
          rayonRecherche = rayonRecherche // FIXME : unité
        )

      for {
        candidat <- optCandidatAuthentifieRequest.candidatAuthentifie.map(c =>
          candidatQueryHandler.handle(CandidatPourRechercheOffreQuery(c.candidatId)).map(Some(_))
        ).getOrElse(Future.successful(None))
      } yield {
        val form = RechercheOffresForm.form.fill(RechercheOffresForm(
          motsCles = None,
          localisation = buildLocalisationOffresFromRequest.orElse(candidat.flatMap(_.localisationRecherche).map(l =>
            LocalisationOffresForm(
              lieuTravail = l.commune,
              codePostal = l.codePostal,
              rayonRecherche = l.rayonRecherche.map(_.value) // FIXME : unité
            ))),
          typesContrats = Nil,
          metiers = Nil,
          page = None
        ))

        Ok(views.html.candidat.rechercheOffres(
          candidatAuthentifie = optCandidatAuthentifieRequest.candidatAuthentifie,
          jsData = Json.obj(
            "candidatAuthentifie" -> candidat.isDefined,
            "cv" -> candidat.exists(_.cv),
            "metiersValides" -> candidat.map(_.metiersValides),
            "csrfToken" -> CSRF.getToken.map(_.value),
            "rechercheFormData" -> form.value,
            "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
          ),
          gtmDataLayer = TrackingService.buildTrackingCandidat(
            optCandidatAuthentifie = optCandidatAuthentifieRequest.candidatAuthentifie,
            flash = Some(messagesRequest.flash)
          )
        ))
      }
    }(optCandidatAuthentifieRequest)
  }

  def rechercherOffres: Action[AnyContent] = optionalCandidatAuthentifieAction.async { request: OptionalCandidatAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def buildCriteresRechercheOffre(rechercheOffresForm: RechercheOffresForm,
                                      secteursActivites: List[SecteurActivite]): CriteresRechercheOffre =
        CriteresRechercheOffre(
          motsCles = rechercheOffresForm.motsCles.map(_.split(" ").toList).getOrElse(Nil),
          codePostal = rechercheOffresForm.localisation.map(_.codePostal),
          rayonRecherche = rechercheOffresForm.localisation.flatMap(_.rayonRecherche.map(RayonRecherche(_, uniteLongueur = UniteLongueur.KM))),
          typesContrats = rechercheOffresForm.typesContrats.flatMap(TypeContrat.from),
          secteursActivites = Nil, // FIXME : à renseigner
          codesROME =
            if (rechercheOffresForm.metiers.nonEmpty)
              rechercheOffresForm.metiers.map(CodeROME)
            else if (rechercheOffresForm.motsCles.isEmpty)
              secteursActivites.flatMap(_.metiers.map(_.codeROME))
            else
              Nil,
          codesDomaineProfessionnels = Nil, // FIXME : à renseigner,
          page = rechercheOffresForm.page.map(p =>
            PageOffres(debut = p.debut, fin = p.fin)
          )
        )

      RechercheOffresForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        rechercheOffresForm =>
          for {
            secteursActivitesQueryResult <- metierQueryHandler.handle(SecteursActiviteQuery)
            offresCandidatQueryResult <- candidatQueryHandler.handle(OffresCandidatQuery(buildCriteresRechercheOffre(rechercheOffresForm, secteursActivitesQueryResult.secteursActivites)))
          } yield
            Ok(Json.toJson(offresCandidatQueryResult))
      )
    }(request)
  }
}
