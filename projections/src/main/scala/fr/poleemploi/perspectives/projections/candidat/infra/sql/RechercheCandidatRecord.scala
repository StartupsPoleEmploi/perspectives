package fr.poleemploi.perspectives.projections.candidat.infra.sql

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain._

case class RechercheCandidatRecord(candidatId: CandidatId,
                                   nom: String,
                                   prenom: String,
                                   email: Email,
                                   commune: Option[String],
                                   metiersEvalues: List[CodeROME],
                                   metiersRecherches: List[CodeROME],
                                   rayonRecherche: Option[RayonRecherche],
                                   numeroTelephone: Option[NumeroTelephone],
                                   cvId: Option[CVId],
                                   cvTypeMedia: Option[TypeMedia])
