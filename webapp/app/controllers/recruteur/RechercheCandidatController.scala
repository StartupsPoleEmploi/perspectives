package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, Coordonnees}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.cv.CVCandidatPourRecruteurQuery
import fr.poleemploi.perspectives.projections.metier.{MetierQueryHandler, MetierRechercheParCodeROMEQuery, SecteurActiviteParCodeQuery, SecteursActiviteQuery}
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.projections.recruteur.alerte.AlertesRecruteurQuery
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.commentaire.domain.ContexteRecherche
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import play.filters.csrf.CSRF

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RechercheCandidatController @Inject()(cc: ControllerComponents,
                                            implicit val assets: AssetsFinder,
                                            implicit val webAppConfig: WebAppConfig,
                                            messagesAction: MessagesActionBuilder,
                                            candidatQueryHandler: CandidatQueryHandler,
                                            recruteurQueryHandler: RecruteurQueryHandler,
                                            recruteurCommandHandler: RecruteurCommandHandler,
                                            metierQueryHandler: MetierQueryHandler,
                                            recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index(secteurActivite: Option[String], metier: Option[String],
            localisation: Option[String], latitude: Option[String], longitude: Option[String]): Action[AnyContent] =
    recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
      messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
        val rechercheCandidatForm = RechercheCandidatForm(
          secteurActivite = secteurActivite,
          metier = metier,
          coordonnees =
            for {
              latitude <- latitude
              longitude <- longitude
            } yield Coordonnees(latitude = latitude.toDouble, longitude = longitude.toDouble),
          pagination = None
        )
        (for {
          alertesRecruteurQueryResult <- recruteurQueryHandler.handle(AlertesRecruteurQuery(recruteurAuthentifieRequest.recruteurId))
          typeRecruteur <- getTypeRecruteur(recruteurAuthentifieRequest)
          query = RechercheCandidatsQuery(
            typeRecruteur = typeRecruteur,
            codeSecteurActivite = rechercheCandidatForm.secteurActivite.map(CodeSecteurActivite),
            codeROME = rechercheCandidatForm.metier.map(CodeROME),
            coordonnees = rechercheCandidatForm.coordonnees,
            nbPagesACharger = 4,
            page = None
          )
          rechercheCandidatQueryResult <- candidatQueryHandler.handle(query)
          secteursActiviteQueryResult <- metierQueryHandler.handle(SecteursActiviteQuery)
          metierChoisi <- rechercheCandidatForm.metier
            .map(m => metierQueryHandler.handle(MetierRechercheParCodeROMEQuery(CodeROME(m))).map(r => Some(r.metier)))
            .getOrElse(Future.successful(None))
          secteurActiviteChoisi <- rechercheCandidatForm.secteurActivite
            .map(s => metierQueryHandler.handle(SecteurActiviteParCodeQuery(CodeSecteurActivite(s))).map(r => Some(r.secteurActiviteDTO)))
            .getOrElse(Future.successful(None))
        } yield {
          Ok(views.html.recruteur.rechercheCandidat(
            rechercheCandidatForm = RechercheCandidatForm.form.fill(rechercheCandidatForm),
            recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
            rechercheCandidatQueryResult = rechercheCandidatQueryResult,
            metierChoisi = metierChoisi,
            secteurActiviteChoisi = secteurActiviteChoisi,
            secteursActivites = secteursActiviteQueryResult.secteursActivites,
            jsData = Json.obj(
              "secteurActivite" -> rechercheCandidatForm.secteurActivite,
              "secteursActivites" -> secteursActiviteQueryResult.secteursActivites,
              "metier" -> rechercheCandidatForm.metier,
              "localisation" -> rechercheCandidatForm.coordonnees.map(c => Json.obj(
                "label" -> localisation,
                "latitude" -> c.latitude,
                "longitude" -> c.longitude
              )),
              "alertes" -> alertesRecruteurQueryResult.alertes,
              "nbCandidatsTotal" -> rechercheCandidatQueryResult.nbCandidatsTotal,
              "nbCandidats" -> rechercheCandidatQueryResult.nbCandidats,
              "nbCandidatsParPage" -> query.nbCandidatsParPage,
              "pagesInitiales" -> rechercheCandidatQueryResult.pages,
              "csrfToken" -> CSRF.getToken.map(_.value),
              "algoliaPlacesConfig" -> webAppConfig.algoliaPlacesConfig
            )
          ))
        }).recover {
          case _: ProfilRecruteurIncompletException =>
            Redirect(routes.ProfilController.modificationProfil())
              .flashing(messagesRequest.flash.withMessageErreur("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche"))
        }
      }(recruteurAuthentifieRequest)
    }

  def rechercherCandidats: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      RechercheCandidatForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        rechercheCandidatForm => {
          (for {
            typeRecruteur <- getTypeRecruteur(recruteurAuthentifieRequest)
            query = RechercheCandidatsQuery(
              typeRecruteur = typeRecruteur,
              codeSecteurActivite = rechercheCandidatForm.secteurActivite.map(CodeSecteurActivite),
              codeROME = rechercheCandidatForm.metier.map(CodeROME),
              coordonnees = rechercheCandidatForm.coordonnees,
              nbPagesACharger = rechercheCandidatForm.pagination.map(_ => 1).getOrElse(4),
              page = rechercheCandidatForm.pagination.map(p => KeysetRechercherCandidats(
                score = p.score,
                dateInscription = p.dateInscription,
                candidatId = Some(CandidatId(p.candidatId))
              ))
            )
            rechercheCandidatQueryResult <- candidatQueryHandler.handle(query)
            secteursActiviteQueryResult <- metierQueryHandler.handle(SecteursActiviteQuery)
            metierChoisi <- rechercheCandidatForm.metier
              .map(m => metierQueryHandler.handle(MetierRechercheParCodeROMEQuery(CodeROME(m))).map(r => Some(r.metier)))
              .getOrElse(Future.successful(None))
            secteurActiviteChoisi <- rechercheCandidatForm.secteurActivite
              .map(s => metierQueryHandler.handle(SecteurActiviteParCodeQuery(CodeSecteurActivite(s))).map(r => Some(r.secteurActiviteDTO)))
              .getOrElse(Future.successful(None))
          } yield {
            Ok(
              Json.obj(
                "html" -> views.html.recruteur.partials.resultatsRecherche(
                  rechercheCandidatQueryResult = rechercheCandidatQueryResult,
                  metierChoisi = metierChoisi,
                  secteurActiviteChoisi = secteurActiviteChoisi,
                  secteursActivites = secteursActiviteQueryResult.secteursActivites,
                ).body.replaceAll("\n", ""),
                "nbCandidatsTotal" -> rechercheCandidatQueryResult.nbCandidatsTotal,
                "nbCandidats" -> rechercheCandidatQueryResult.nbCandidats,
                "pageSuivante" -> rechercheCandidatQueryResult.pageSuivante,
                "pages" -> rechercheCandidatQueryResult.pages
              )
            )
          }).recover {
            case _: ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def getTypeRecruteur(request: RecruteurAuthentifieRequest[AnyContent]): Future[TypeRecruteur] =
    request.flash.getTypeRecruteur
      .map(t => Future.successful(t))
      .getOrElse {
        recruteurQueryHandler.handle(TypeRecruteurQuery(request.recruteurId)).map(_.typeRecruteur.getOrElse(throw ProfilRecruteurIncompletException()))
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
          contentType = Some(cvCandidat.cv.typeMedia.value))
      )
    }).recover {
      case _: UnauthorizedQueryException =>
        Redirect(routes.LandingController.landing()).flashing(recruteurAuthentifieRequest.flash.withMessageErreur("Vous n'êtes pas autorisé à accéder à cette ressource"))
    }
  }

  def commenterListeCandidats: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      CommenterListeCandidatsForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        commenterListeCandidatsForm => {
          recruteurCommandHandler.handle(
            CommenterListeCandidatsCommand(
              id = recruteurAuthentifieRequest.recruteurId,
              contexteRecherche = ContexteRecherche(
                secteurActivite = commenterListeCandidatsForm.secteurActiviteRecherche,
                metier = commenterListeCandidatsForm.metierRecherche,
                localisation = commenterListeCandidatsForm.localisationRecherche
              ),
              commentaire = commenterListeCandidatsForm.commentaire
            )
          ).map(_ => NoContent)
        }
      )
    }(recruteurAuthentifieRequest)
  }


}

case class ProfilRecruteurIncompletException() extends Exception
