package fr.poleemploi.perspectives.domain.candidat

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

sealed trait CandidatEvent extends Event

case class CandidatInscrisEvent(nom: String,
                                prenom: String,
                                email: String,
                                genre: Option[Genre],
                                date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class CriteresRechercheModifiesEvent(rechercheMetierEvalue: Boolean,
                                          rechercheAutreMetier: Boolean,
                                          metiersRecherches: Set[Metier],
                                          etreContacteParOrganismeFormation: Boolean,
                                          etreContacteParAgenceInterim: Boolean,
                                          rayonRecherche: RayonRecherche,
                                          date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class ProfilCandidatModifiePEConnectEvent(nom: String,
                                               prenom: String,
                                               email: String,
                                               genre: Genre,
                                               date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class NumeroTelephoneModifieEvent(numeroTelephone: NumeroTelephone,
                                       date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class AdressePEConnectModifieeEvent(adresse: Adresse,
                                         date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent

case class StatutDemandeurEmploiPEConnectModifieEvent(statutDemandeurEmploi: StatutDemandeurEmploi,
                                                      date: ZonedDateTime = ZonedDateTime.now()) extends CandidatEvent