package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain._

sealed trait RecruteurEvent extends Event {

  def recruteurId: RecruteurId
}

case class RecruteurInscritEvent(recruteurId: RecruteurId,
                                 nom: Nom,
                                 prenom: Prenom,
                                 email: Email,
                                 genre: Genre) extends RecruteurEvent

case class RecruteurConnecteEvent(recruteurId: RecruteurId) extends RecruteurEvent

case class ProfilModifieEvent(recruteurId: RecruteurId,
                              typeRecruteur: TypeRecruteur,
                              raisonSociale: String,
                              numeroSiret: NumeroSiret,
                              numeroTelephone: NumeroTelephone,
                              contactParCandidats: Boolean) extends RecruteurEvent

case class AdresseRecruteurModifieeEvent(recruteurId: RecruteurId,
                                         adresse: Adresse) extends RecruteurEvent

case class ProfilGerantModifieEvent(recruteurId: RecruteurId,
                                    nom: Nom,
                                    prenom: Prenom,
                                    email: Email,
                                    genre: Genre) extends RecruteurEvent