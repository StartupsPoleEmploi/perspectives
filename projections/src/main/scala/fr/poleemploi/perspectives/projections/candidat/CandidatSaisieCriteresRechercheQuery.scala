package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._

case class CandidatSaisieCriteresRechercheQuery(candidatId: CandidatId) extends Query[CandidatSaisieCriteresRechercheQueryResult]

case class CandidatSaisieCriteresRechercheQueryResult(candidatId: CandidatId,
                                                      nom: Nom,
                                                      prenom: Prenom,
                                                      rechercheMetierEvalue: Option[Boolean],
                                                      metiersEvalues: List[Metier],
                                                      rechercheAutreMetier: Option[Boolean],
                                                      metiersRecherches: List[CodeROME],
                                                      contacteParAgenceInterim: Option[Boolean],
                                                      contacteParOrganismeFormation: Option[Boolean],
                                                      rayonRecherche: Option[RayonRecherche],
                                                      numeroTelephone: Option[NumeroTelephone],
                                                      cvId: Option[CVId],
                                                      cvTypeMedia: Option[TypeMedia]) extends QueryResult {

  def possedeCV: Boolean = cvId.isDefined

  def nomCV: Option[String] = cvTypeMedia.map(t => s"${prenom.value} ${nom.value}.${TypeMedia.getExtensionFichier(t)}")
}
