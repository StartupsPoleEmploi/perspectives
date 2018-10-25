package authentification.infra.play

import play.api.mvc.Session

object SessionRecruteurNonAuthentifie {

  private val namespace = "recruteurNonAuthentifie"
  private val uriConnexionAttribute: String = s"$namespace.uriConnexion"

  def getUriConnexion(session: Session): Option[String] =
    session.get(uriConnexionAttribute)

  def setUriConnexion(uriConnexion: String,
                      session: Session): Session =
    session + (uriConnexionAttribute -> uriConnexion)

  def remove(session: Session): Session =
    session - uriConnexionAttribute
}
