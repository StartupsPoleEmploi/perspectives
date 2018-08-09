package controllers

import play.api.mvc.Flash

object FlashMessages {

  val keyMessageSucces = "message_succes"
  val keyMessageErreur = "message_erreur"

  val keyInscriptionRecruteur = "recruteur_inscris"
  val keyInscriptionCandidat = "candidat_inscris"

  implicit class FlashMessage[T](f: Flash) {

    def hasMessages: Boolean = f.get(keyMessageSucces).isDefined || f.get(keyMessageErreur).isDefined

    def getMessageSucces: Option[String] = f.get(keyMessageSucces)
    def withMessageSucces(message: String): Flash = f + (keyMessageSucces -> message)

    def getMessageErreur: Option[String] = f.get(keyMessageErreur)
    def withMessageErreur(message: String): Flash = f + (keyMessageErreur -> message)

    def recruteurInscris: Boolean = f.get(keyInscriptionRecruteur).contains("true")
    def withRecruteurInscris: Flash = f + (keyInscriptionRecruteur -> "true")

    def candidatInscris: Boolean = f.get(keyInscriptionCandidat).contains("true")
    def withCandidatInscris: Flash = f + (keyInscriptionCandidat -> "true")
  }
}