package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.commun.domain.{Genre, NumeroTelephone}

sealed trait RecruteurEvent extends Event {

  def recruteurId: RecruteurId
}

case class RecruteurInscrisEvent(recruteurId: RecruteurId,
                                 nom: String,
                                 prenom: String,
                                 email: String,
                                 genre: Genre) extends RecruteurEvent

case class ProfilModifieEvent(recruteurId: RecruteurId,
                              typeRecruteur: TypeRecruteur,
                              raisonSociale: String,
                              numeroSiret: NumeroSiret,
                              numeroTelephone: NumeroTelephone,
                              contactParCandidats: Boolean) extends RecruteurEvent

case class ProfilRecruteurModifiePEConnectEvent(recruteurId: RecruteurId,
                                                nom: String,
                                                prenom: String,
                                                email: String,
                                                genre: Genre) extends RecruteurEvent