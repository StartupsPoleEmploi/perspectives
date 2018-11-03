package controllers

import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.mvc.Flash

object FlashMessages {

  val keyMessageSucces = "message_succes"
  val keyMessageErreur = "message_erreur"

  val keyInscriptionRecruteur = "recruteur_inscrit"
  val keyConnexionRecruteur = "recruteur_connecte"
  val keyTypeRecruteur = "type_recruteur"
  val keyInscriptionCandidat = "candidat_inscrit"
  val keyConnexionCandidat = "candidat_connecte"

  implicit class FlashMessage[T](f: Flash) {

    def hasMessages: Boolean = f.get(keyMessageSucces).isDefined || f.get(keyMessageErreur).isDefined

    def getMessageSucces: Option[String] = f.get(keyMessageSucces)
    def withMessageSucces(message: String): Flash = f + (keyMessageSucces -> message)

    def getMessageErreur: Option[String] = f.get(keyMessageErreur)
    def withMessageErreur(message: String): Flash = f + (keyMessageErreur -> message)

    def recruteurInscrit: Boolean = f.get(keyInscriptionRecruteur).contains("true")
    def withRecruteurInscrit: Flash = f + (keyInscriptionRecruteur -> "true")

    def recruteurConnecte: Boolean = f.get(keyConnexionRecruteur).contains("true")
    def withRecruteurConnecte: Flash = f + (keyConnexionRecruteur -> "true")

    def getTypeRecruteur: Option[TypeRecruteur] = f.get(keyTypeRecruteur).map(TypeRecruteur(_))
    def withTypeRecruteur(typeRecruteur: TypeRecruteur): Flash = f + (keyTypeRecruteur -> typeRecruteur.value)

    def candidatInscrit: Boolean = f.get(keyInscriptionCandidat).contains("true")
    def withCandidatInscrit: Flash = f + (keyInscriptionCandidat -> "true")

    def candidatConnecte: Boolean = f.get(keyConnexionCandidat).contains("true")
    def withCandidatConnecte: Flash = f + (keyConnexionCandidat -> "true")
  }
}