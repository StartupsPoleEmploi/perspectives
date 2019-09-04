package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, _}
import fr.poleemploi.perspectives.conseiller.ConseillerId

sealed trait CandidatEvent extends Event {

  def candidatId: CandidatId
}

case class CandidatInscritEvent(candidatId: CandidatId,
                                nom: Nom,
                                prenom: Prenom,
                                email: Email,
                                genre: Genre) extends CandidatEvent

case class CandidatConnecteEvent(candidatId: CandidatId) extends CandidatEvent

case class CandidatAutologgeEvent(candidatId: CandidatId) extends CandidatEvent

case class ProfilCandidatModifieEvent(candidatId: CandidatId,
                                      nom: Nom,
                                      prenom: Prenom,
                                      email: Email,
                                      genre: Genre) extends CandidatEvent

case class AdresseModifieeEvent(candidatId: CandidatId,
                                adresse: Adresse,
                                coordonnees: Coordonnees) extends CandidatEvent

case class StatutDemandeurEmploiModifieEvent(candidatId: CandidatId,
                                             statutDemandeurEmploi: StatutDemandeurEmploi) extends CandidatEvent

case class LanguesModifieesEvent(candidatId: CandidatId,
                                 langues: List[Langue]) extends CandidatEvent

case class CentresInteretModifiesEvent(candidatId: CandidatId,
                                       centresInteret: List[CentreInteret]) extends CandidatEvent

case class FormationsModifieesEvent(candidatId: CandidatId,
                                    formations: List[Formation]) extends CandidatEvent

case class ExperiencesProfessionnellesModifieesEvent(candidatId: CandidatId,
                                                     experiencesProfessionnelles: List[ExperienceProfessionnelle]) extends CandidatEvent

case class PermisModifiesEvent(candidatId: CandidatId,
                               permis: List[Permis]) extends CandidatEvent

case class SavoirEtreModifiesEvent(candidatId: CandidatId,
                                   savoirEtre: List[SavoirEtre]) extends CandidatEvent

case class SavoirFaireModifiesEvent(candidatId: CandidatId,
                                    savoirFaire: List[SavoirFaire]) extends CandidatEvent

case class VisibiliteRecruteurModifieeEvent(candidatId: CandidatId,
                                            contactRecruteur: Boolean,
                                            contactFormation: Boolean) extends CandidatEvent

case class CriteresRechercheModifiesEvent(candidatId: CandidatId,
                                          localisationRecherche: LocalisationRecherche,
                                          codesROMEValidesRecherches: Set[CodeROME],
                                          codesROMERecherches: Set[CodeROME],
                                          codesDomaineProfessionnelRecherches: Set[CodeDomaineProfessionnel],
                                          tempsTravailRecherche: TempsTravail) extends CandidatEvent

case class NumeroTelephoneModifieEvent(candidatId: CandidatId,
                                       numeroTelephone: NumeroTelephone) extends CandidatEvent

case class CVAjouteEvent(candidatId: CandidatId,
                         cvId: CVId,
                         typeMedia: TypeMedia) extends CandidatEvent

case class CVRemplaceEvent(candidatId: CandidatId,
                           cvId: CVId,
                           typeMedia: TypeMedia) extends CandidatEvent

case class MRSAjouteeEvent(candidatId: CandidatId,
                           codeROME: CodeROME,
                           departement: CodeDepartement,
                           habiletes: Set[Habilete],
                           dateEvaluation: LocalDate,
                           isDHAE: Boolean) extends CandidatEvent

case class RepriseEmploiDeclareeParConseillerEvent(candidatId: CandidatId,
                                                   conseillerId: ConseillerId) extends CandidatEvent
