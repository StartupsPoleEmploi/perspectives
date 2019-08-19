package authentification.infra.peconnect

import authentification._
import conf.WebAppConfig
import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.CandidatAuthentifie
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAuthAdapter
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectCandidatInfos
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectAccessTokenStorage, PEConnectAdapter}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatSaisieCriteresRechercheQuery}
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PEConnectCandidatController @Inject()(cc: ControllerComponents,
                                            webAppConfig: WebAppConfig,
                                            candidatNonAuthentifieAction: CandidatNonAuthentifieAction,
                                            candidatAuthentifieAction: CandidatAuthentifieAction,
                                            candidatAuthentifiePEConnectAction: CandidatAuthentifiePEConnectAction,
                                            candidatCommandHandler: CandidatCommandHandler,
                                            candidatQueryHandler: CandidatQueryHandler,
                                            peConnectAuthAdapter: PEConnectAuthAdapter,
                                            peConnectAdapter: PEConnectAdapter,
                                            peConnectAccessTokenStorage: PEConnectAccessTokenStorage)(implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  val authentificationPEConnect = new AuthPEConnect(
    peConnectAuthAdapter = peConnectAuthAdapter,
    oauthConfig = webAppConfig.candidatOauthConfig,
    connexionCallbackURL = authentification.infra.peconnect.routes.PEConnectCandidatController.connexionCallback(),
    deconnexionCallbackURL = authentification.infra.peconnect.routes.PEConnectCandidatController.deconnexionCallback()
  )

  def connexion: Action[AnyContent] = candidatNonAuthentifieAction { implicit request =>
    SessionOauthTokensCandidat.getOauthTokensCandidat(request.session)
      .map(oauthTokens =>
        authentificationPEConnect.connexion(oauthTokens)
      ).getOrElse(
      Redirect(authentification.infra.peconnect.routes.PEConnectCandidatController.connexion())
        .withSession(SessionOauthTokensCandidat.setOauthTokensCandidat(peConnectAuthAdapter.generateTokens, request.session))
    )
  }

  def connexionCallback: Action[AnyContent] = candidatNonAuthentifieAction.async { implicit request =>
    authentificationPEConnect.connexionCallback(
      oauthTokens = SessionOauthTokensCandidat.getOauthTokensCandidat(request.session).getOrElse(throw new IllegalArgumentException("Aucun token n'a été stocké en session")),
      () => Future(
        Redirect(controllers.candidat.routes.LandingController.landing())
          .withSession(SessionOauthTokensCandidat.removeOauthTokensCandidat(request.session))
      ),
      accessTokenResponse =>
        (for {
          infosCandidat <- peConnectAdapter.infosCandidat(accessTokenResponse.accessToken)
          optCandidatPEConnect <- peConnectAdapter.findCandidat(infosCandidat.peConnectId)
          candidatId = optCandidatPEConnect.map(_.candidatId).getOrElse(candidatCommandHandler.newId)
          _ <- peConnectAccessTokenStorage.add(candidatId, accessTokenResponse)
          _ <- optCandidatPEConnect
            .map(c =>
              if (webAppConfig.candidatsPEConnectTesteurs.contains(c.peConnectId))
                Future.successful(())
              else
                connecter(c.candidatId, infosCandidat)
            ).getOrElse(inscrire(candidatId, infosCandidat))
          optCriteresRecherche <- optCandidatPEConnect.map(c => candidatQueryHandler.handle(CandidatSaisieCriteresRechercheQuery(c.candidatId)).map(Some(_))).getOrElse(Future.successful(None))
        } yield {
          val candidatAuthentifie = CandidatAuthentifie(
            candidatId = candidatId,
            nom = infosCandidat.nom,
            prenom = infosCandidat.prenom
          )
          val session = SessionCandidatPEConnect.setJWTToken(accessTokenResponse.idToken, SessionCandidatAuthentifie.set(candidatAuthentifie, SessionOauthTokensCandidat.removeOauthTokensCandidat(request.session)))

          if (optCriteresRecherche.exists(_.saisieComplete))
            SessionUtilisateurNonAuthentifie.getUriConnexion(request.session)
              .map(uri => Redirect(uri).withSession(SessionUtilisateurNonAuthentifie.remove(session)))
              .getOrElse(Redirect(controllers.candidat.routes.RechercheOffreController.index()).withSession(session))
          else if (optCriteresRecherche.isDefined)
            Redirect(controllers.candidat.routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
              .flashing(request.flash.withMessageAlerte("Veuillez finaliser la saisie de vos critères"))
          else
            Redirect(controllers.candidat.routes.SaisieCriteresRechercheController.saisieCriteresRecherche()).withSession(session)
              .flashing(request.flash.withCandidatInscrit)
        }).recover {
          case CandidatPEConnectEmailManquantException =>
            Redirect(controllers.candidat.routes.LandingController.landing())
              .withSession(SessionOauthTokensCandidat.removeOauthTokensCandidat(request.session))
              .flashing(request.flash.withMessageErreur("Veuillez réessayer après avoir renseigner votre adresse email dans votre profil en vous connectant sur https://candidat.pole-emploi.fr"))
        }
    ).recover {
      case t: Throwable =>
        logger.error(s"Erreur lors du callback candidat PEConnect avec la requete ${request.rawQueryString}", t)
        // Nettoyage de session et redirect
        Redirect(controllers.candidat.routes.LandingController.landing())
          .withSession(SessionOauthTokensCandidat.removeOauthTokensCandidat(request.session))
          .flashing(request.flash.withMessageErreur("Notre service en actuellement en cours de maintenance, veuillez réessayer ultérieurement."))
    }
  }

  def deconnexion: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatAuthentifiePEConnectAction { implicit candidatAuthentifiePEConnectRequest: CandidatAuthentifiePEConnectRequest[AnyContent] =>
      authentificationPEConnect.deconnexion
    }(candidatAuthentifieRequest)
  }

  def deconnexionCallback: Action[AnyContent] = candidatAuthentifieAction.async { candidatAuthentifieRequest: CandidatAuthentifieRequest[AnyContent] =>
    candidatAuthentifiePEConnectAction.async { implicit candidatAuthentifiePEConnectRequest: CandidatAuthentifiePEConnectRequest[AnyContent] =>
      peConnectAccessTokenStorage.remove(candidatAuthentifieRequest.candidatId).map(_ =>
        // Nettoyage de session et redirect
        Redirect(controllers.candidat.routes.LandingController.landing()).withSession(
          SessionCandidatAuthentifie.remove(SessionCandidatPEConnect.remove(candidatAuthentifiePEConnectRequest.session))
        )
      )
    }(candidatAuthentifieRequest)
  }

  private def inscrire(candidatId: CandidatId,
                       peConnectCandidatInfos: PEConnectCandidatInfos): Future[Unit] = {
    val command = InscrireCandidatCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email.getOrElse(throw CandidatPEConnectEmailManquantException)
    )
    for {
      _ <- peConnectAdapter.saveCandidat(CandidatPEConnect(
        candidatId = candidatId,
        peConnectId = peConnectCandidatInfos.peConnectId
      ))
      _ <- candidatCommandHandler.handle(command)
    } yield ()
  }

  private def connecter(candidatId: CandidatId,
                        peConnectCandidatInfos: PEConnectCandidatInfos): Future[Unit] =
    candidatCommandHandler.handle(ConnecterCandidatCommand(
      id = candidatId,
      nom = peConnectCandidatInfos.nom,
      prenom = peConnectCandidatInfos.prenom,
      genre = peConnectCandidatInfos.genre,
      email = peConnectCandidatInfos.email.getOrElse(throw CandidatPEConnectEmailManquantException)
    ))
}

case object CandidatPEConnectEmailManquantException extends Exception