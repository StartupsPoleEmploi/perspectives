package authentification

import play.api.mvc.Session

case class AuthenticatedCandidat(candidatId: String,
                                 idTokenPEConnect: Option[String],
                                 nom: String,
                                 prenom: String)

object AuthenticatedCandidat {

  def buildFromSession(session: Session): Option[AuthenticatedCandidat] =
    for {
      candidatId <- session.get("candidatId")
      nom <- session.get("nom")
      prenom <- session.get("prenom")
    } yield AuthenticatedCandidat(
      candidatId = candidatId,
      idTokenPEConnect = session.get("idTokenPEConnect"),
      nom = nom,
      prenom = prenom
    )

  def storeInSession(authenticatedCandidat: AuthenticatedCandidat,
                     session: Session): Session = {
    val result = session + ("candidatId" -> authenticatedCandidat.candidatId) + ("nom" -> authenticatedCandidat.nom) + ("prenom" -> authenticatedCandidat.prenom)

    if (authenticatedCandidat.idTokenPEConnect.isDefined) {
      result + ("idTokenPEConnect" -> authenticatedCandidat.idTokenPEConnect.get)
    } else result
  }
}