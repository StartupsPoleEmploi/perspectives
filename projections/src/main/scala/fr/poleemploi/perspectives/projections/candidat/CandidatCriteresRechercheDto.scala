package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.commun.domain._

case class CandidatCriteresRechercheDto(candidatId: CandidatId,
                                        nom: String,
                                        prenom: String,
                                        rechercheMetierEvalue: Option[Boolean],
                                        rechercheAutreMetier: Option[Boolean],
                                        metiersRecherches: List[CodeROME],
                                        contacteParAgenceInterim: Option[Boolean],
                                        contacteParOrganismeFormation: Option[Boolean],
                                        rayonRecherche: Option[RayonRecherche],
                                        numeroTelephone: Option[NumeroTelephone],
                                        cvId: Option[CVId]) {

  def possedeCV: Boolean = cvId.isDefined
}
