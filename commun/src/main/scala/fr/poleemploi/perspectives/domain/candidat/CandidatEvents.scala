package fr.poleemploi.perspectives.domain.candidat

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.Event

sealed trait CandidatEvent extends Event

case class CandidatInscrisEvent(nom: String,
                                prenom: String,
                                email: String,
                                genre: Option[String],
                                date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class CriteresRechercheModifiesEvent(rechercheMetierEvalue: Boolean,
                                          rechercheAutreMetier: Boolean,
                                          listeMetiersRecherches: Set[String],
                                          etreContacteParOrganismeFormation: Boolean,
                                          etreContacteParAgenceInterim: Boolean,
                                          rayonRecherche: Int,
                                          date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class ProfilCandidatModifiePEConnectEvent(nom: String,
                                               prenom: String,
                                               email: String,
                                               genre: String,
                                               date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent