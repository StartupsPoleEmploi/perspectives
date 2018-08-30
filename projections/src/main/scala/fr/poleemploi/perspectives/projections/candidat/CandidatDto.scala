package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._

case class CandidatDto(candidatId: CandidatId,
                       nom: String,
                       prenom: String,
                       genre: Genre,
                       email: String,
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

  /**
    * Ne se base pas sur statutDemandeurEmploi car il n'est pas forcément actualisé tout de suite
    * par le candidat et cela implique une reconnexion du candidat via un service externe.
    */
  def rechercheEmploi: Boolean =
    (rechercheMetierEvalue.isEmpty && rechercheAutreMetier.isEmpty) ||
      rechercheMetierEvalue.getOrElse(false) || rechercheAutreMetier.getOrElse(false)

  def metiersRecherchesParSecteur: Map[SecteurActivite, List[Metier]] =
    metiersRecherches.flatMap(Metier.from).groupBy(m => SecteurActivite.parMetier(m.codeROME))

  def habiletes: List[Habilete] = metiersEvalues.flatMap(Metier.from).flatMap(_.habiletes).distinct
}
