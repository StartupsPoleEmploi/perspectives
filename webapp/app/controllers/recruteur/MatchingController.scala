package controllers.recruteur

import akka.stream.scaladsl.Source
import akka.util.ByteString
import authentification.infra.play.{RecruteurAuthentifieAction, RecruteurAuthentifieRequest}
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.cqrs.projection.UnauthorizedQueryException
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Metier, SecteurActivite}
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.recruteur.RecruteurId
import javax.inject.{Inject, Singleton}
import play.api.http.HttpEntity
import play.api.mvc.{Action, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MatchingController @Inject()(cc: ControllerComponents,
                                   implicit val webAppConfig: WebAppConfig,
                                   messagesAction: MessagesActionBuilder,
                                   candidatQueryHandler: CandidatQueryHandler,
                                   recruteurQueryHandler: RecruteurQueryHandler,
                                   recruteurAuthentifieAction: RecruteurAuthentifieAction) extends AbstractController(cc) {

  def index(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      val matchingForm = MatchingForm(
        secteurActivite = None,
        metier = None
      )
      rechercher(matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map { resultatRechercheCandidatDto =>
        MatchingForm.form.fill(matchingForm)
        Ok(views.html.recruteur.matching(MatchingForm.form.fill(matchingForm), recruteurAuthentifieRequest.recruteurAuthentifie, resultatRechercheCandidatDto))
      }.recover {
        case _: ProfilRecruteurIncompletException =>
          Redirect(routes.ProfilController.modificationProfil())
            .flashing(messagesRequest.flash.withMessageErreur("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche"))
      }
    }(recruteurAuthentifieRequest)
  }

  def rechercherCandidats(): Action[AnyContent] = recruteurAuthentifieAction.async { recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    messagesAction.async { implicit messagesRequest: MessagesRequest[AnyContent] =>
      MatchingForm.form.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(formWithErrors.errorsAsJson))
        },
        matchingForm => {
          rechercher(matchingForm = matchingForm, recruteurId = recruteurAuthentifieRequest.recruteurId).map(resultatRechercheCandidatDto =>
            Ok(views.html.recruteur.partials.resultatsRecherche(resultatRechercheCandidatDto))
          ).recover {
            case _: ProfilRecruteurIncompletException => BadRequest("Vous devez renseigner votre profil avant de pouvoir effectuer une recherche")
          }
        }
      )
    }(recruteurAuthentifieRequest)
  }

  private def rechercher(matchingForm: MatchingForm, recruteurId: RecruteurId): Future[ResultatRechercheCandidat] =
    getRecruteurAvecProfilComplet(recruteurId).flatMap(recruteurDto =>
      if (matchingForm.metier.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParMetier(RechercherCandidatsParMetierQuery(
          metier = matchingForm.metier.flatMap(Metier.from).get,
          typeRecruteur = recruteurDto.typeRecruteur.get
        ))
      } else if (matchingForm.secteurActivite.exists(_.nonEmpty)) {
        candidatQueryHandler.rechercherCandidatsParSecteur(RechercheCandidatsParSecteurQuery(
          secteurActivite = matchingForm.secteurActivite.flatMap(SecteurActivite.from).get,
          typeRecruteur = recruteurDto.typeRecruteur.get
        ))
      } else {
        candidatQueryHandler.rechercheCandidatsParDateInscription(RechercherCandidatsParDateInscriptionQuery(
          typeRecruteur = recruteurDto.typeRecruteur.get
        ))
      })

  private def getRecruteurAvecProfilComplet(recruteurId: RecruteurId): Future[RecruteurDto] =
    recruteurQueryHandler.getRecruteur(GetRecruteurQuery(recruteurId)).map(r => if (!r.profilComplet) throw ProfilRecruteurIncompletException() else r)

  def telechargerCV(candidatId: String): Action[AnyContent] = recruteurAuthentifieAction.async { implicit recruteurAuthentifieRequest: RecruteurAuthentifieRequest[AnyContent] =>
    candidatQueryHandler.getCVPourRecruteurParCandidat(GetCVPourRecruteurParCandidatQuery(
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
          contentType = Some(fichierCv.typeMedia))
      )
    }).recover {
      case _: UnauthorizedQueryException =>
        Redirect(routes.LandingController.landing()).flashing(recruteurAuthentifieRequest.flash.withMessageErreur("Vous n'êtes pas autorisé à accéder à cette ressource"))
    }
  }

}

case class ProfilRecruteurIncompletException() extends Exception
