package authentification.infra.peconnect

import fr.poleemploi.perspectives.commun.infra.oauth.OauthTokens
import play.api.mvc.Session

/**
  * Utilisé par le controleur pour stocker les tokens oauth le temps de l'authentification.
  */
object SessionOauthTokens {

  private val candidatStateAttribute: String = s"candidat.oauth.state"
  private val candidatNonceAttribute: String = s"candidat.oauth.nonce"
  private val recruteurStateAttribute: String = s"recruteur.oauth.nonce"
  private val recruteurNonceAttribute: String = s"recruteur.oauth.nonce"

  def setOauthTokensCandidat(oauthTokens: OauthTokens, session: Session): Session =
    session + (candidatStateAttribute -> oauthTokens.state) + (candidatNonceAttribute -> oauthTokens.nonce)

  def getOauthTokensCandidat(session: Session): Option[OauthTokens] =
    for {
      state <- session.get(candidatStateAttribute)
      nonce <- session.get(candidatNonceAttribute)
    } yield OauthTokens(
      state = state,
      nonce = nonce
    )

  def removeOauthTokensCandidat(session: Session): Session =
    session - candidatStateAttribute - candidatNonceAttribute

  def setOauthTokensRecruteur(oauthTokens: OauthTokens, session: Session): Session =
    session + (recruteurStateAttribute -> oauthTokens.state) + (recruteurNonceAttribute -> oauthTokens.nonce)

  def getOauthTokensRecruteur(session: Session): Option[OauthTokens] =
    for {
      state <- session.get(recruteurStateAttribute)
      nonce <- session.get(recruteurNonceAttribute)
    } yield OauthTokens(
      state = state,
      nonce = nonce
    )

  def removeOauthTokensRecruteur(session: Session): Session =
    session - recruteurStateAttribute - recruteurNonceAttribute

}