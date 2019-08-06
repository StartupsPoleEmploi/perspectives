package controllers

import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.mvc.Flash

object FlashMessages {

  private val keyMessageSucces = "message_succes"
  private val keyMessageAlerte = "message_alerte"
  private val keyMessageErreur = "message_erreur"

  private val keyInscriptionRecruteur = "recruteur_inscrit"
  private val keyTypeRecruteur = "type_recruteur"

  private val keyInscriptionCandidat = "candidat_inscrit"
  private val keyNouveauCandidat = "nouveau_candidat"
  private val keyLocalisationRechercheCandidat = "candidat_localisation_recherche_modifiee"

  implicit class FlashMessage[T](f: Flash) {

    def hasMessages: Boolean = f.get(keyMessageSucces).isDefined || f.get(keyMessageAlerte).isDefined || f.get(keyMessageErreur).isDefined

    def getMessageSucces: Option[String] = f.get(keyMessageSucces)
    def withMessageSucces(message: String): Flash = f + (keyMessageSucces -> message)

    def getMessageAlerte: Option[String] = f.get(keyMessageAlerte)
    def withMessageAlerte(message: String): Flash = f + (keyMessageAlerte -> message)

    def getMessageErreur: Option[String] = f.get(keyMessageErreur)
    def withMessageErreur(message: String): Flash = f + (keyMessageErreur -> message)

    def recruteurInscrit: Boolean = f.get(keyInscriptionRecruteur).contains("true")
    def withRecruteurInscrit: Flash = f + (keyInscriptionRecruteur -> "true")

    def getTypeRecruteur: Option[TypeRecruteur] = f.get(keyTypeRecruteur).map(TypeRecruteur(_))
    def withTypeRecruteur(typeRecruteur: TypeRecruteur): Flash = f + (keyTypeRecruteur -> typeRecruteur.value)

    def candidatInscrit: Boolean = f.get(keyInscriptionCandidat).contains("true")
    def withCandidatInscrit: Flash = f + (keyInscriptionCandidat -> "true")

    def nouveauCandidat: Boolean = f.get(keyInscriptionCandidat).contains("true")
    def withNouveauCandidat: Flash = f + (keyInscriptionCandidat -> "true")
  }
}