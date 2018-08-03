package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}

sealed trait RecruteurEvent extends Event

case class RecruteurInscrisEvent(nom: String,
                                 prenom: String,
                                 email: String,
                                 genre: Genre) extends RecruteurEvent

case class ProfilModifieEvent(typeRecruteur: TypeRecruteur,
                              raisonSociale: String,
                              numeroSiret: NumeroSiret,
                              numeroTelephone: NumeroTelephone,
                              contactParCandidats: Boolean) extends RecruteurEvent

case class ProfilRecruteurModifiePEConnectEvent(nom: String,
                                                prenom: String,
                                                email: String,
                                                genre: Genre) extends RecruteurEvent