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

case class CriteresRechercheModifiesEvent(candidatId: CandidatId,
                                          rechercheMetierEvalue: Boolean,
                                          rechercheAutreMetier: Boolean,
                                          metiersRecherches: Set[CodeROME],
                                          etreContacteParOrganismeFormation: Boolean,
                                          etreContacteParAgenceInterim: Boolean,
                                          rayonRecherche: RayonRecherche) extends CandidatEvent

case class ProfilCandidatModifieEvent(candidatId: CandidatId,
                                      nom: Nom,
                                      prenom: Prenom,
                                      email: Email,
                                      genre: Genre) extends CandidatEvent

case class NumeroTelephoneModifieEvent(candidatId: CandidatId,
                                       numeroTelephone: NumeroTelephone) extends CandidatEvent

case class AdresseModifieeEvent(candidatId: CandidatId,
                                adresse: Adresse,
                                coordonnees: Option[Coordonnees]) extends CandidatEvent

case class StatutDemandeurEmploiModifieEvent(candidatId: CandidatId,
                                             statutDemandeurEmploi: StatutDemandeurEmploi) extends CandidatEvent

case class CVAjouteEvent(candidatId: CandidatId,
                         cvId: CVId,
                         typeMedia: TypeMedia) extends CandidatEvent

case class CVRemplaceEvent(candidatId: CandidatId,
                           cvId: CVId,
                           typeMedia: TypeMedia) extends CandidatEvent

case class MRSAjouteeEvent(candidatId: CandidatId,
                           metier: CodeROME,
                           departement: CodeDepartement,
                           habiletes: List[Habilete],
                           dateEvaluation: LocalDate) extends CandidatEvent

case class RepriseEmploiDeclareeParConseillerEvent(candidatId: CandidatId,
                                                   conseillerId: ConseillerId) extends CandidatEvent