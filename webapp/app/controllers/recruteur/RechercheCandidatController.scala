package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification._
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, Coordonnees}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.cv.CVCandidatPourRecruteurQuery
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.rome.domain.ReferentielRome
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RechercheCandidatController @Inject()(cc: ControllerComponents,
                                            implicit val assets: AssetsFinder,
                                            implicit val webAppConfig: WebAppConfig,
                                            messagesAction: MessagesActionBuilder,
                                            candidatQueryHandler: CandidatQueryHandler,
                                            recruteurQueryHandler: RecruteurQueryHandler,
                                            recruteurCommandHandler: RecruteurCommandHandler,
                                            referentielRome: ReferentielRome,
                                            recruteurAuthentifieAction: RecruteurAuthentifieAction,
                                            recruteurAConnecterSiNonAuthentifieAction: RecruteurAConnecterSiNonAuthentifieAction)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  private val MIN_CARACTERES_RECHERCHE_PAR_METIER = 2

  def index(codeRome: Option[String] = None, latitude: Option[Double] = None, longitude: Option[Double] = None): Action[AnyContent] = recruteurAConnecterSiNonAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      def buildCoordonneesFromRequest: Option[Coordonnees] =
        for {
          latitude <- latitude
          longitude <- longitude
        } yield Coordonnees(
          latitude = latitude,
          longitude = longitude
        )

      (for {
        typeRecruteur <- getTypeRecruteur(recruteurAuthentifieRequest)
        query = RechercheCandidatsQuery(
          typeRecruteur = typeRecruteur,
          codeSecteurActivite = codeRome.map(CodeROME(_).codeSecteurActivite).orElse(None),
          codeROME = codeRome.map(CodeROME).orElse(None),
          coordonnees = buildCoordonneesFromRequest.orElse(None),
          nbPagesACharger = 4,
          page = None
        )
        rechercheCandidatQueryResult <- candidatQueryHandler.handle(query)
        secteursActivitesAvecCandidatsQueryResult <- candidatQueryHandler.handle(SecteursActivitesAvecCandidatsQuery(typeRecruteur))
      } yield {
        Ok(views.html.recruteur.rechercheCandidats(
          recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
          jsData = Json.obj(
            "secteursActivites" -> secteursActivitesAvecCandidatsQueryResult.secteursActivites,
            "resultatRecherche" -> rechercheCandidatQueryResult,
            "nbCandidatsParPage" -> query.nbCandidatsParPage,
            "csrfToken" -> CSRF.getToken.map(_.value),
            "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
          )
        ))
      }).recover {
        case ProfilRecruteurIncompletException =>
          Redirect(routes.ProfilController.modificationProfil())
            .flashing(messagesRequest.flash.withMessageErreur("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche"))
      }
    }(recruteurAuthentifieRequest)
  }

  def rechercherMetiers(q: String): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      if (q.isEmpty || q.trim.length < MIN_CARACTERES_RECHERCHE_PAR_METIER) Future.successful(BadRequest)
      else recruteurQueryHandler.handle(MetiersRecruteurQuery(q)).map(metiersRecruteurQueryResult =>
        Ok(Json.toJson(metiersRecruteurQueryResult))
      )
    }(recruteurAuthentifieRequest)
  }

  def rechercherCandidats: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      RechercheCandidatForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        rechercheCandidatForm =>
          (for {
            typeRecruteur <- getTypeRecruteur(recruteurAuthentifieRequest)
            rechercheCandidatQueryResult <- candidatQueryHandler.handle(RechercheCandidatsQuery(
              typeRecruteur = typeRecruteur,
              codeSecteurActivite = rechercheCandidatForm.secteurActivite.map(CodeSecteurActivite),
              codeROME = rechercheCandidatForm.metier.map(CodeROME),
              coordonnees = rechercheCandidatForm.coordonnees,
              nbPagesACharger = rechercheCandidatForm.pagination.map(_ => 1).getOrElse(4),
              page = rechercheCandidatForm.pagination.map(p => KeysetCandidatPourRecruteur(
                score = p.score,
                dateInscription = p.dateInscription,
                candidatId = CandidatId(p.candidatId)
              ))
            ))
          } yield {
            Ok(Json.toJson(rechercheCandidatQueryResult))
          }).recover {
            case ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
      )
    }(recruteurAuthentifieRequest)
  }

  private def getTypeRecruteur(request: RecruteurAuthentifieRequest[AnyContent]): Future[TypeRecruteur] =
    request.flash.getTypeRecruteur
      .map(t => Future.successful(t))
      .getOrElse {
        recruteurQueryHandler.handle(TypeRecruteurQuery(request.recruteurId)).map(_.typeRecruteur.getOrElse(throw ProfilRecruteurIncompletException))
      }

  def telechargerCV(candidatId: String, nomFichier: String): Action[AnyContent] = recruteurAuthentifieAction.async { implicit recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.handle(CVCandidatPourRecruteurQuery(
      candidatId = CandidatId(candidatId),
      recruteurId = recruteurAuthentifieRequest.recruteurId
    )).map(cvCandidat => {
      val source: Source[ByteString, _] = Source.fromIterator[ByteString](
        () => Iterator.fill(1)(ByteString(cvCandidat.cv.data))
      )

      Result(
        header = ResponseHeader(200, Map(
          "Content-Disposition" -> "inline"
        )),
        body = HttpEntity.Streamed(
          data = source,
          contentLength = Some(cvCandidat.cv.data.length.toLong),
          contentType = Some(cvCandidat.cv.typeMedia.value)
        )
      )
    }).recover {
      case _: UnauthorizedQueryException =>
        Redirect(routes.LandingController.landing()).flashing(recruteurAuthentifieRequest.flash.withMessageErreur("Vous n'êtes pas autorisé à accéder à cette ressource"))
    }
  }
}

case object ProfilRecruteurIncompletException extends Exception
