package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.AssetsFinder
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur.commentaire.domain.ContexteRecherche
import fr.poleemploi.perspectives.recruteur.{CommenterListeCandidatsCommand, RecruteurCommandHandler, TypeRecruteur}
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.mvc.{Action, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(cc: ControllerComponents,
                                   implicit val assets: AssetsFinder,
                                   implicit val webAppConfig: WebAppConfig,
                                   messagesAction: MessagesActionBuilder,
                                   candidatQueryHandler: CandidatQueryHandler,
                                   recruteurQueryHandler: RecruteurQueryHandler,
                                   recruteurCommandHandler: RecruteurCommandHandler,
                                   rechercheCandidatQueryHandler: RechercheCandidatQueryHandler,
                                   recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val departementsProposes = rechercheCandidatQueryHandler.departementsProposes
      val matchingForm = MatchingForm(
        secteurActivite = None,
        metier = None,
        codeDepartement = departementsProposes.headOption.map(_.code)
      )
      rechercher(request = recruteurAuthentifieRequest, matchingForm = matchingForm).map(resultatRechercheCandidatDto =>
        Ok(views.html.recruteur.matching(
          matchingForm = MatchingForm.form.fill(matchingForm),
          recruteurAuthentifie = recruteurAuthentifieRequest.recruteurAuthentifie,
          resultatRechercheCandidat = resultatRechercheCandidatDto,
          secteursActivites = rechercheCandidatQueryHandler.secteursProposes,
          departements = departementsProposes
        ))
      ).recover {
        case _: ProfilRecruteurIncompletException =>
          Redirect(routes.ProfilController.modificationProfil())
            .flashing(messagesRequest.flash.withMessageErreur("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche"))
      }
    }(recruteurAuthentifieRequest)
  }

  def rechercherCandidats: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      MatchingForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(formWithErrors.errorsAsJson))
        },
        matchingForm => {
          rechercher(request = recruteurAuthentifieRequest, matchingForm = matchingForm).map(resultatRechercheCandidatDto =>
            Ok(views.html.recruteur.partials.resultatsRecherche(
              resultatRechercheCandidat = resultatRechercheCandidatDto,
              metierChoisi = matchingForm.metier.flatMap(c => rechercheCandidatQueryHandler.metierProposeParCode(CodeROME(c)).map(_.label)),
              secteurActiviteChoisi = matchingForm.secteurActivite.map(s => rechercheCandidatQueryHandler.secteurProposeParCode(CodeSecteurActivite(s)).label)
            ))
          ).recover {
            case _: ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def rechercher(request: RecruteurAuthentifieRequest[AnyContent],
                         matchingForm: MatchingForm): Future[ResultatRechercheCandidat] =
    getTypeRecruteur(request).flatMap(typeRecruteur =>
      if (matchingForm.metier.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParMetier(RechercherCandidatsParMetierQuery(
          codeROME = matchingForm.metier.map(CodeROME).get,
          typeRecruteur = typeRecruteur,
          codeDepartement = matchingForm.codeDepartement
        ))
      } else if (matchingForm.secteurActivite.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParSecteur(RechercherCandidatsParSecteurQuery(
          codeSecteurActivite = matchingForm.secteurActivite.map(CodeSecteurActivite(_)).get,
          typeRecruteur = typeRecruteur,
          codeDepartement = matchingForm.codeDepartement
        ))
      } else {
        candidatQueryHandler.rechercherCandidatsParDateInscription(RechercherCandidatsParDateInscriptionQuery(
          typeRecruteur = typeRecruteur,
          codeDepartement = matchingForm.codeDepartement
        ))
      })

  private def getTypeRecruteur(request: RecruteurAuthentifieRequest[AnyContent]): Future[TypeRecruteur] =
    request.flash.getTypeRecruteur
      .map(t => Future.successful(t))
      .getOrElse {
        recruteurQueryHandler.typeRecruteur(request.recruteurId).map(_.getOrElse(throw ProfilRecruteurIncompletException()))
      }

  def telechargerCV(candidatId: String, nomFichier: String): Action[AnyContent] = recruteurAuthentifieAction.async { implicit recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.cvCandidatPourRecruteur(CVCandidatPourRecruteurQuery(
      candidatId = CandidatId(candidatId),
      recruteurId = recruteurAuthentifieRequest.recruteurId
    )).map(fichierCv => {
      val source: Source[ByteString, _] = Source.fromIterator[ByteString](
        () => Iterator.fill(1)(ByteString(fichierCv.data))
      )

      Result(
        header = ResponseHeader(200, Map(
          "Content-Disposition" -> "inline"
        )),
        body = HttpEntity.Streamed(
          data = source,
          contentLength = Some(fichierCv.data.length.toLong),
          contentType = Some(fichierCv.typeMedia.value))
      )
    }).recover {
      case _: UnauthorizedQueryException =>
        Redirect(routes.LandingController.landing()).flashing(recruteurAuthentifieRequest.flash.withMessageErreur("Vous n'êtes pas autorisé à accéder à cette ressource"))
    }
  }

  def commenterListeCandidats: Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      CommenterListeCandidatsForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(formWithErrors.errorsAsJson))
        },
        commenterListeCandidatsForm => {
          recruteurCommandHandler.commenterListeCandidats(
            CommenterListeCandidatsCommand(
              id = recruteurAuthentifieRequest.recruteurId,
              contexteRecherche = ContexteRecherche(
                secteurActivite = commenterListeCandidatsForm.secteurActiviteRecherche.map(s => rechercheCandidatQueryHandler.secteurProposeParCode(CodeSecteurActivite(s))),
                metier = commenterListeCandidatsForm.metierRecherche.flatMap(c => rechercheCandidatQueryHandler.metierProposeParCode(CodeROME(c))),
                departement = commenterListeCandidatsForm.departementRecherche.map(rechercheCandidatQueryHandler.departementParCode)
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
