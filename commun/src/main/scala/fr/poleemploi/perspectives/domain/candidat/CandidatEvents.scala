package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

sealed trait CandidatEvent extends Event {

  def candidatId: CandidatId
}

case class CandidatInscrisEvent(candidatId: CandidatId,
                                nom: String,
                                prenom: String,
                                email: String,
                                genre: Option[Genre]) extends CandidatEvent

case class CriteresRechercheModifiesEvent(candidatId: CandidatId,
                                          rechercheMetierEvalue: Boolean,
                                          rechercheAutreMetier: Boolean,
                                          metiersRecherches: Set[Metier],
                                          etreContacteParOrganismeFormation: Boolean,
                                          etreContacteParAgenceInterim: Boolean,
                                          rayonRecherche: RayonRecherche) extends CandidatEvent

case class ProfilCandidatModifiePEConnectEvent(candidatId: CandidatId,
                                               nom: String,
                                               prenom: String,
                                               email: String,
                                               genre: Genre) extends CandidatEvent

case class NumeroTelephoneModifieEvent(candidatId: CandidatId,
                                       numeroTelephone: NumeroTelephone) extends CandidatEvent

case class AdressePEConnectModifieeEvent(candidatId: CandidatId,
                                         adresse: Adresse) extends CandidatEvent

case class StatutDemandeurEmploiPEConnectModifieEvent(candidatId: CandidatId,
                                                      statutDemandeurEmploi: StatutDemandeurEmploi) extends CandidatEvent