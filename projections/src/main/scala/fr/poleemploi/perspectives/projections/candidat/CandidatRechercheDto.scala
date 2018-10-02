package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._

case class CandidatRechercheDto(candidatId: CandidatId,
                                nom: String,
                                prenom: String,
                                email: Email,
                                commune: Option[String],
                                metiersEvalues: List[Metier],
                                habiletes: List[Habilete],
                                metiersRecherchesParSecteur: Map[SecteurActivite, List[Metier]],
                                rayonRecherche: Option[RayonRecherche],
                                numeroTelephone: Option[NumeroTelephone],
                                cvId: Option[CVId],
                                cvTypeMedia: Option[TypeMedia]) {

  def possedeCV: Boolean = cvId.isDefined

  def nomCV: Option[String] = cvTypeMedia.map(t => s"$nom-$prenom.${TypeMedia.getExtensionFichier(t)}")
}
