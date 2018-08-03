package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

sealed trait CandidatEvent extends Event

case class CandidatInscrisEvent(nom: String,
                                prenom: String,
                                email: String,
                                genre: Option[Genre]) extends CandidatEvent

case class CriteresRechercheModifiesEvent(rechercheMetierEvalue: Boolean,
                                          rechercheAutreMetier: Boolean,
                                          metiersRecherches: Set[Metier],
                                          etreContacteParOrganismeFormation: Boolean,
                                          etreContacteParAgenceInterim: Boolean,
                                          rayonRecherche: RayonRecherche) extends CandidatEvent

case class ProfilCandidatModifiePEConnectEvent(nom: String,
                                               prenom: String,
                                               email: String,
                                               genre: Genre) extends CandidatEvent

case class NumeroTelephoneModifieEvent(numeroTelephone: NumeroTelephone) extends CandidatEvent

case class AdressePEConnectModifieeEvent(adresse: Adresse) extends CandidatEvent

case class StatutDemandeurEmploiPEConnectModifieEvent(statutDemandeurEmploi: StatutDemandeurEmploi) extends CandidatEvent