package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.cv.CVCandidatPourRecruteurQuery
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.projections.recruteur.alerte.AlertesRecruteurQuery
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte}
import fr.poleemploi.perspectives.recruteur.commentaire.domain.ContexteRecherche
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.libs.json.{JsString, Json}
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
                                            rechercheCandidatQueryHandler: RechercheCandidatQueryHandler,
                                            recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index(secteurActivite: Option[String], metier: Option[String], departement: Option[String]): Action[AnyContent] =
    recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
      messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
        val departementsProposes = rechercheCandidatQueryHandler.departementsProposes
        val rechercheCandidatForm = RechercheCandidatForm(
          secteurActivite = secteurActivite,
          metier = metier,
          codeDepartement = departement.orElse(departementsProposes.headOption.map(_.code.value))
        )
        (for {
          alertesRecruteurQueryResult <- recruteurQueryHandler.handle(AlertesRecruteurQuery(recruteurAuthentifieRequest.recruteurId))
          rechercheCandidatQueryResult <- rechercher(request = recruteurAuthentifieRequest, rechercheCandidatForm = rechercheCandidatForm)
        } yield {
          Ok(views.html.recruteur.rechercheCandidat(
            rechercheCandidatForm = RechercheCandidatForm.form.fill(rechercheCandidatForm),
            recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
            rechercheCandidatQueryResult = rechercheCandidatQueryResult,
            metierChoisi = rechercheCandidatForm.metier.flatMap(c => rechercheCandidatQueryHandler.metierProposeParCode(CodeROME(c)).map(_.label)),
            secteurActiviteChoisi = rechercheCandidatForm.secteurActivite.map(s => rechercheCandidatQueryHandler.secteurProposeParCode(CodeSecteurActivite(s)).label),
            jsData = Json.obj(
              "secteurActivite" -> Json.toJson(rechercheCandidatForm.secteurActivite.getOrElse("")),
              "secteursActivites" -> Json.toJson(rechercheCandidatQueryHandler.secteursProposes),
              "metier" -> Json.toJson(rechercheCandidatForm.metier.getOrElse("")),
              "metiers" -> Json.toJson(rechercheCandidatQueryHandler.secteursProposes.flatMap(_.metiers)),
              "departement" -> Json.toJson(rechercheCandidatForm.codeDepartement.getOrElse("")),
              "departements" -> Json.toJson(departementsProposes),
              "alertes" -> Json.toJson(alertesRecruteurQueryResult.alertes),
              "nbCandidats" -> rechercheCandidatQueryResult.nbCandidats,
              "csrfToken" -> JsString(CSRF.getToken.map(_.value).getOrElse(""))
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
          rechercher(request = recruteurAuthentifieRequest, rechercheCandidatForm = rechercheCandidatForm).map(rechercheCandidatQueryResult =>
            Ok(
              Json.obj(
                "html" -> views.html.recruteur.partials.resultatsRecherche(
                  rechercheCandidatQueryResult = rechercheCandidatQueryResult,
                  metierChoisi = rechercheCandidatForm.metier.flatMap(c => rechercheCandidatQueryHandler.metierProposeParCode(CodeROME(c)).map(_.label)),
                  secteurActiviteChoisi = rechercheCandidatForm.secteurActivite.map(s => rechercheCandidatQueryHandler.secteurProposeParCode(CodeSecteurActivite(s)).label)
                ).body.replaceAll("\n", ""),
                "nbCandidats" -> rechercheCandidatQueryResult.nbCandidats
              ))
            ).recover {
            case _: ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def rechercher(request: RecruteurAuthentifieRequest[AnyContent],
                         rechercheCandidatForm: RechercheCandidatForm): Future[RechercheCandidatQueryResult] =
    for {
      typeRecruteur <- getTypeRecruteur(request)
      rechercheCandidatQuery = rechercheCandidatForm.metier.filter(_.nonEmpty).map(code =>
        RechercherCandidatsParMetierQuery(
          codeROME = CodeROME(code),
          typeRecruteur = typeRecruteur,
          codeDepartement = rechercheCandidatForm.codeDepartement.map(CodeDepartement)
        )
      ).orElse(rechercheCandidatForm.secteurActivite.filter(_.nonEmpty).map(code =>
        RechercherCandidatsParSecteurQuery(
          codeSecteurActivite = CodeSecteurActivite(code),
          typeRecruteur = typeRecruteur,
          codeDepartement = rechercheCandidatForm.codeDepartement.map(CodeDepartement)
        )
      )).orElse(rechercheCandidatForm.codeDepartement.filter(_.nonEmpty).map(code =>
        RechercherCandidatsParDepartementQuery(
          typeRecruteur = typeRecruteur,
          codeDepartement = CodeDepartement(code)
        )
      )).getOrElse(throw new IllegalArgumentException("Filtre de recherche non géré"))
      rechercheCandidatQueryResult <- candidatQueryHandler.handle(rechercheCandidatQuery)
    } yield rechercheCandidatQueryResult

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
                secteurActivite = commenterListeCandidatsForm.secteurActiviteRecherche.map(s => rechercheCandidatQueryHandler.secteurProposeParCode(CodeSecteurActivite(s))),
                metier = commenterListeCandidatsForm.metierRecherche.flatMap(c => rechercheCandidatQueryHandler.metierProposeParCode(CodeROME(c))),
                departement = commenterListeCandidatsForm.departementRecherche.map(c => rechercheCandidatQueryHandler.departementParCode(CodeDepartement(c)))
              ),
              commentaire = commenterListeCandidatsForm.commentaire
            )
          ).map(_ => NoContent)
        }
      )
    }(recruteurAuthentifieRequest)
  }

  def creerAlerte: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      CreerAlerteForm.form.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(formWithErrors.errorsAsJson)),
        creerAlerteForm => {
          val alerteId = recruteurCommandHandler.newAlerteId
          recruteurCommandHandler.handle(
            CreerAlerteCommand(
              id = recruteurAuthentifieRequest.recruteurId,
              alerteId = alerteId,
              codeSecteurActivite = creerAlerteForm.secteurActivite.map(CodeSecteurActivite(_)),
              codeROME = creerAlerteForm.metier.map(CodeROME),
              codeDepartement = creerAlerteForm.codeDepartement.map(CodeDepartement),
              frequenceAlerte = FrequenceAlerte.frequenceAlerte(creerAlerteForm.frequence).get
            )
          ).map(_ => Created(alerteId.value))
        }
      )
    }(recruteurAuthentifieRequest)
  }

  def supprimerAlerte(alerteId: String): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      recruteurCommandHandler.handle(SupprimerAlerteCommand(
        id = recruteurAuthentifieRequest.recruteurId,
        alerteId = AlerteId(alerteId)
      )).map(_ => NoContent)
    }(recruteurAuthentifieRequest)
  }
}

case class ProfilRecruteurIncompletException() extends Exception
