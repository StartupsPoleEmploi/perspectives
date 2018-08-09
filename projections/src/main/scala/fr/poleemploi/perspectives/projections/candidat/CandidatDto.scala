package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

case class CandidatDto(candidatId: CandidatId,
                       nom: String,
                       prenom: String,
                       genre: Option[Genre],
                       email: String,
                       statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                       rechercheMetierEvalue: Option[Boolean],
                       metiersEvalues: List[Metier],
                       rechercheAutreMetier: Option[Boolean],
                       metiersRecherches: List[Metier],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[RayonRecherche],
                       numeroTelephone: Option[NumeroTelephone],
                       cvId: Option[CVId],
                       dateInscription: ZonedDateTime) {

  def hasCV: Boolean = cvId.isDefined
}
