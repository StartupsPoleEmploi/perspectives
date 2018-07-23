package fr.poleemploi.perspectives.domain.recruteur

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.Event

sealed trait RecruteurEvent extends Event

case class RecruteurInscrisEvent(nom: String,
                                 prenom: String,
                                 email: String,
                                 genre: String,
                                 date: ZonedDateTime = ZonedDateTime.now()) extends RecruteurEvent

case class ProfilModifieEvent(typeRecruteur: String,
                              raisonSociale: String,
                              numeroSiret: String,
                              numeroTelephone: String,
                              contactParCandidats: Boolean,
                              date: ZonedDateTime = ZonedDateTime.now()) extends RecruteurEvent

case class ProfilRecruteurModifiePEConnectEvent(nom: String,
                                                prenom: String,
                                                email: String,
                                                genre: String,
                                                date: ZonedDateTime = ZonedDateTime.now()) extends RecruteurEvent