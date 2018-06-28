package authentification.infra.peconnect

import play.api.mvc.Session

/**
  * Utilisé par le controleur pour stocker les tokens oauth le temps de l'authentification. <br />
  * On ne peut pas totalement l'encapsuler dans PEConnectService car le controleur doit manipuler la Session directement.
  * Le namespace est utilisé si plusieurs composants utilisent la session pour PEConnect afin d'éviter les conflits
  */
class OauthTokenSessionStorage(val namespace: String) {

  private val stateAttribute: String = s"$namespace.oauth.state"
  private val nonceAttribute: String = s"$namespace.oauth.nonce"

  def set(oauthTokens: OauthTokens, session: Session): Session =
    session + (stateAttribute -> oauthTokens.state) + (nonceAttribute -> oauthTokens.nonce)

  def get(session: Session): Option[OauthTokens] =
    for {
      state <- session.get(stateAttribute)
      nonce <- session.get(nonceAttribute)
    } yield OauthTokens(
      state = state,
      nonce = nonce
    )

  def remove(session: Session): Session = session - stateAttribute - nonceAttribute

}
