package fr.poleemploi.perspectives.domain.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}

sealed trait RecruteurEvent extends Event

case class RecruteurInscrisEvent(nom: String,
                                 prenom: String,
                                 email: String,
                                 genre: Genre,
                                 date: ZonedDateTime = ZonedDateTime.now()) extends RecruteurEvent

case class ProfilModifieEvent(typeRecruteur: TypeRecruteur,
                              raisonSociale: String,
                              numeroSiret: NumeroSiret,
                              numeroTelephone: NumeroTelephone,
                              contactParCandidats: Boolean,
                              date: ZonedDateTime = ZonedDateTime.now()) extends RecruteurEvent

case class ProfilRecruteurModifiePEConnectEvent(nom: String,
                                                prenom: String,
                                                email: String,
                                                genre: Genre,
                                                date: ZonedDateTime = ZonedDateTime.now()) extends RecruteurEvent