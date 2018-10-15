package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte}

sealed trait RecruteurEvent extends Event {

  def recruteurId: RecruteurId
}

case class RecruteurInscritEvent(recruteurId: RecruteurId,
                                 nom: String,
                                 prenom: String,
                                 email: Email,
                                 genre: Genre) extends RecruteurEvent

case class RecruteurConnecteEvent(recruteurId: RecruteurId) extends RecruteurEvent

case class ProfilModifieEvent(recruteurId: RecruteurId,
                              typeRecruteur: TypeRecruteur,
                              raisonSociale: String,
                              numeroSiret: NumeroSiret,
                              numeroTelephone: NumeroTelephone,
                              contactParCandidats: Boolean) extends RecruteurEvent

case class ProfilGerantModifieEvent(recruteurId: RecruteurId,
                                    nom: String,
                                    prenom: String,
                                    email: Email,
                                    genre: Genre) extends RecruteurEvent

case class AlerteRecruteurCreeEvent(recruteurId: RecruteurId,
                                    prenom: String,
                                    typeRecruteur: TypeRecruteur,
                                    email: Email,
                                    alerteId: AlerteId,
                                    frequence: FrequenceAlerte,
                                    codeROME: Option[CodeROME],
                                    codeSecteurActivite: Option[CodeSecteurActivite],
                                    codeDepartement: Option[CodeDepartement]) extends RecruteurEvent

case class AlerteRecruteurSupprimeeEvent(recruteurId: RecruteurId,
                                         alerteId: AlerteId) extends RecruteurEvent