package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.domain._
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.candidat.{CandidatId, StatutDemandeurEmploi}

case class CandidatDto(candidatId: CandidatId,
                       nom: String,
                       prenom: String,
                       genre: Option[Genre],
                       email: String,
                       statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                       codePostal: Option[String],
                       commune: Option[String],
                       rechercheMetierEvalue: Option[Boolean],
                       metiersEvalues: List[Metier],
                       rechercheAutreMetier: Option[Boolean],
                       metiersRecherches: List[Metier],
                       contacteParAgenceInterim: Option[Boolean],
                       contacteParOrganismeFormation: Option[Boolean],
                       rayonRecherche: Option[RayonRecherche],
                       numeroTelephone: Option[NumeroTelephone],
                       cvId: Option[CVId],
                       dateInscription: ZonedDateTime,
                       indexerMatching: Boolean) {

  def hasCV: Boolean = cvId.isDefined

  /**
    * Ne se base pas sur statutDemandeurEmploi car il n'est pas forcément actualisé tout de suite
    * par le candidat et cela implique une reconnexion au service via PEConnect.
    */
  def rechercheEmploi: Boolean =
    (rechercheMetierEvalue.isEmpty && rechercheAutreMetier.isEmpty) ||
      rechercheMetierEvalue.getOrElse(false) || rechercheAutreMetier.getOrElse(false)

  def metiersRecherchesParSecteur: Map[SecteurActivite, List[Metier]] =
    metiersRecherches.groupBy(m => SecteurActivite.getSecteur(m).orNull)

  def habiletes: List[Habilete] =
    metiersEvalues.flatMap(_.habiletes).distinct
}
