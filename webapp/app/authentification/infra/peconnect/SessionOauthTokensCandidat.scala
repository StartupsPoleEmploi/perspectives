package authentification.infra.peconnect

import fr.poleemploi.perspectives.commun.infra.oauth.OauthTokens
import play.api.mvc.Session

object SessionOauthTokensCandidat {

  private val candidatStateAttribute: String = s"candidat.oauth.state"
  private val candidatNonceAttribute: String = s"candidat.oauth.nonce"

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
}