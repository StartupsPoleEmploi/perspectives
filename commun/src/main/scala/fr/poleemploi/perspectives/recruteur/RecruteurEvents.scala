package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}

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