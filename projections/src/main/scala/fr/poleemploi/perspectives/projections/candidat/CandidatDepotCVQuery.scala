package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}

case class CandidatDepotCVQuery(candidatId: CandidatId) extends Query[CandidatDepotCVQueryResult]

case class CandidatDepotCVQueryResult(candidatId: CandidatId,
                                      nom: Nom,
                                      prenom: Prenom,
                                      cvId: Option[CVId],
                                      cvTypeMedia: Option[TypeMedia]) extends QueryResult
