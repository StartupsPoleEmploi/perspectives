package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._

case class CandidatPourConseillerDto(candidatId: CandidatId,
                                     nom: String,
                                     prenom: String,
                                     genre: Genre,
                                     email: Email,
                                     statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                     rechercheMetierEvalue: Option[Boolean],
                                     metiersEvalues: List[Metier],
                                     rechercheAutreMetier: Option[Boolean],
                                     metiersRecherches: List[Metier],
                                     contacteParAgenceInterim: Option[Boolean],
                                     contacteParOrganismeFormation: Option[Boolean],
                                     rayonRecherche: Option[RayonRecherche],
                                     numeroTelephone: Option[NumeroTelephone],
                                     dateInscription: ZonedDateTime,
                                     dateDerniereConnexion: ZonedDateTime) {

  /**
    * Ne se base pas sur statutDemandeurEmploi car il n'est pas forcément actualisé tout de suite
    * par le candidat et cela implique une reconnexion du candidat via un service externe.
    */
  def rechercheEmploi: Boolean =
    (rechercheMetierEvalue.isEmpty && rechercheAutreMetier.isEmpty) ||
      rechercheMetierEvalue.getOrElse(false) || rechercheAutreMetier.getOrElse(false)
}