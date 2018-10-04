package fr.poleemploi.perspectives.projections.candidat.infra.sql

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{CodeROME, NumeroTelephone, RayonRecherche}

case class CandidatSaisieCriteresRechercheRecord(candidatId: CandidatId,
                                                 nom: String,
                                                 prenom: String,
                                                 rechercheMetierEvalue: Option[Boolean],
                                                 metiersEvalues: List[CodeROME],
                                                 rechercheAutreMetier: Option[Boolean],
                                                 metiersRecherches: List[CodeROME],
                                                 contacteParAgenceInterim: Option[Boolean],
                                                 contacteParOrganismeFormation: Option[Boolean],
                                                 rayonRecherche: Option[RayonRecherche],
                                                 numeroTelephone: Option[NumeroTelephone],
                                                 cvId: Option[CVId],
                                                 cvTypeMedia: Option[TypeMedia])
