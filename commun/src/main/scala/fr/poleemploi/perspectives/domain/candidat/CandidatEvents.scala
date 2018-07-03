package fr.poleemploi.perspectives.domain.candidat

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.Event

case class CandidatInscrisEvent(peConnectId: String,
                                nom: String,
                                prenom: String,
                                email: String,
                                date: ZonedDateTime = ZonedDateTime.now()) extends Event

case class CriteresRechercheModifiesEvent(rechercheMetierEvalue: Boolean,
                                          rechercheAutreMetier: Boolean,
                                          listeMetiersRecherches: Set[String],
                                          etreContacteParOrganismeFormation: Boolean,
                                          etreContacteParAgenceInterim: Boolean,
                                          rayonRecherche: Int,
                                          date: ZonedDateTime = ZonedDateTime.now()) extends Event