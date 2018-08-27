package fr.poleemploi.perspectives.domain.candidat

import java.time.LocalDate

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.conseiller.ConseillerId
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

sealed trait CandidatEvent extends Event {

  def candidatId: CandidatId
}

case class CandidatInscrisEvent(candidatId: CandidatId,
                                nom: String,
                                prenom: String,
                                email: String,
                                genre: Genre) extends CandidatEvent

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

case class CVAjouteEvent(candidatId: CandidatId,
                         cvId: CVId) extends CandidatEvent

case class CVRemplaceEvent(candidatId: CandidatId,
                           cvId: CVId) extends CandidatEvent

case class MRSAjouteeEvent(candidatId: CandidatId,
                           metier: String, // FIXME : referentiel metier
                           dateEvaluation: LocalDate) extends CandidatEvent

case class RepriseEmploiDeclareeParConseillerEvent(candidatId: CandidatId,
                                                   conseillerId: ConseillerId) extends CandidatEvent