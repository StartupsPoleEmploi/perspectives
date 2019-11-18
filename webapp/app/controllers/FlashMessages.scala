package controllers

import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.mvc.Flash

object FlashMessages {

  private val keyMessageSucces = "message_succes"
  private val keyMessageAlerte = "message_alerte"
  private val keyMessageErreur = "message_erreur"

  private val keyInscriptionRecruteur = "recruteur_inscrit"
  private val keyConnexionRecruteur = "recruteur_connecte"
  private val keyDeconnexionRecruteur = "recruteur_deconnecte"
  private val keyTypeRecruteur = "type_recruteur"

  private val keyInscriptionCandidat = "candidat_inscrit"
  private val keyNouveauCandidat = "nouveau_candidat"
  private val keyLocalisationRechercheCandidat = "candidat_localisation_recherche_modifiee"

  private val keyConnexionCandidat = "candidat_connecte"
  private val keyDeconnexionCandidat = "candidat_deconnecte"
  private val keyAutologinCandidat = "candidat_autologue"

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

    def recruteurConnecte: Boolean = f.get(keyConnexionRecruteur).contains("true")
    def withRecruteurConnecte: Flash = f + (keyConnexionRecruteur -> "true")

    def recruteurDeconnecte: Boolean = f.get(keyDeconnexionRecruteur).contains("true")
    def withRecruteurDeconnecte: Flash = f + (keyDeconnexionRecruteur -> "true")

    def getTypeRecruteur: Option[TypeRecruteur] = f.get(keyTypeRecruteur).map(TypeRecruteur(_))
    def withTypeRecruteur(typeRecruteur: TypeRecruteur): Flash = f + (keyTypeRecruteur -> typeRecruteur.value)

    def candidatInscrit: Boolean = f.get(keyInscriptionCandidat).contains("true")
    def withCandidatInscrit: Flash = f + (keyInscriptionCandidat -> "true")

    def candidatConnecte: Boolean = f.get(keyConnexionCandidat).contains("true")
    def withCandidatConnecte: Flash = f + (keyConnexionCandidat -> "true")

    def candidatDeconnecte: Boolean = f.get(keyDeconnexionCandidat).contains("true")
    def withCandidatDeconnecte: Flash = f + (keyDeconnexionCandidat -> "true")

    def candidatAutologue: Boolean = f.get(keyAutologinCandidat).contains("true")
    def withCandidatAutologue: Flash = f + (keyAutologinCandidat -> "true")

    def nouveauCandidat: Boolean = f.get(keyInscriptionCandidat).contains("true")
    def withNouveauCandidat: Flash = f + (keyInscriptionCandidat -> "true")
  }
}
