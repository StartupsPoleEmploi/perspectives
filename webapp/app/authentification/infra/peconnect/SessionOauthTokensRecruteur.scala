package authentification.infra.peconnect

import fr.poleemploi.perspectives.commun.infra.oauth.OauthTokens
import play.api.mvc.Session

object SessionOauthTokensRecruteur {

  private val recruteurStateAttribute: String = s"recruteur.oauth.nonce"
  private val recruteurNonceAttribute: String = s"recruteur.oauth.nonce"

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