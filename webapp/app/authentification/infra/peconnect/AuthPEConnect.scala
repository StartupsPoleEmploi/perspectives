package authentification.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAuthAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.JWTToken
import fr.poleemploi.perspectives.authentification.infra.peconnect.ws.AccessTokenResponse
import fr.poleemploi.perspectives.commun.EitherUtils._
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthConfig, OauthTokens}
import play.api.mvc.Results.Redirect
import play.api.mvc._
import play.mvc.Http.Status.SEE_OTHER

import scala.concurrent.{ExecutionContext, Future}

trait UtilisateurPEConnectAuthentifieRequest[+A] extends Request[A] {
  def idTokenPEConnect: JWTToken
}

class AuthPEConnect(peConnectAuthAdapter: PEConnectAuthAdapter,
                    oauthConfig: OauthConfig,
                    connexionCallbackURL: Call,
                    deconnexionCallbackURL: Call)(implicit exec: ExecutionContext) {

  def connexion(oauthTokens: OauthTokens)(implicit request: Request[AnyContent]): Result =
    Redirect(
      url = s"${oauthConfig.urlAuthentification}/connexion/oauth2/authorize",
      status = SEE_OTHER,
      queryString = Map(
        "realm" -> Seq(s"/${oauthConfig.realm}"),
        "response_type" -> Seq("code"),
        "client_id" -> Seq(oauthConfig.clientId),
        "scope" -> Seq(OauthConfig.scopes(oauthConfig)),
        "redirect_uri" -> Seq(connexionCallbackURL.absoluteURL()),
        "state" -> Seq(oauthTokens.state),
        "nonce" -> Seq(oauthTokens.nonce)
      )
    )

  def connexionCallback(oauthTokens: OauthTokens,
                        connexionAnnulee: () => Future[Result],
                        utilisateurAuthentifie: AccessTokenResponse => Future[Result])(implicit request: Request[AnyContent]): Future[Result] = {
    def isConnexionAnnulee: Boolean =
      request.queryString.size == 1 && request.getQueryString("state").isDefined

    def authentifier: Future[AccessTokenResponse] =
      for {
        authorizationCode <- request.getQueryString("code").toRight("Aucun code d'autorisation n'a été retourné").toFuture
        stateCallback <- request.getQueryString("state").toRight("Aucun state n'a été retourné").toFuture
        accessTokenResponse <- peConnectAuthAdapter.accessToken(
          authorizationCode = authorizationCode,
          state = stateCallback,
          redirectUri = connexionCallbackURL.absoluteURL(),
          oauthTokens = oauthTokens,
          oauthConfig = oauthConfig
        )
      } yield accessTokenResponse

    if (isConnexionAnnulee)
      connexionAnnulee()
    else
      authentifier.flatMap(utilisateurAuthentifie(_))
  }

  def deconnexion(implicit request: UtilisateurPEConnectAuthentifieRequest[AnyContent]): Result = Redirect(
    url = s"${oauthConfig.urlAuthentification}/compte/deconnexion",
    status = SEE_OTHER,
    queryString = Map(
      "id_token_hint" -> Seq(request.idTokenPEConnect.value),
      "redirect_uri" -> Seq(deconnexionCallbackURL.absoluteURL())
    )
  )
}