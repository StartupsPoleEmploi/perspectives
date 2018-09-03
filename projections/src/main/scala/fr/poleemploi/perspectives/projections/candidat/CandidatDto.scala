package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._

case class CandidatDto(candidatId: CandidatId,
                       nom: String,
                       prenom: String,
                       genre: Genre,
                       email: Email,
                       statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                       codePostal: Option[String],
                       commune: Option[String],
                       rechercheMetierEvalue: Option[Boolean],
                       metiersEvalues: List[CodeROME],
                       rechercheAutreMetier: Option[Boolean],
                       metiersRecherches: List[CodeROME],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[RayonRecherche],
                       numeroTelephone: Option[NumeroTelephone],
                       cvId: Option[CVId],
                       dateInscription: ZonedDateTime,
                       indexerMatching: Boolean) {

  def hasCV: Boolean = cvId.isDefined
}
