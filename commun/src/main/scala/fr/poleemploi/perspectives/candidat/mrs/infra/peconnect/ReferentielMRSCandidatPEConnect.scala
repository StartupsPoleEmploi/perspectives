package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMRSCandidatPEConnect(mrsValideesCandidatsSqlAdapter: MRSValideesCandidatsSqlAdapter,
                                      peConnectSqlAdapter: PEConnectSqlAdapter) extends ReferentielMRSCandidat {

  override def mrsValideesParCandidat(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      mrsValidees <- mrsValideesCandidatsSqlAdapter.mrsValideesParCandidat(candidatPEConnect.peConnectId)
    } yield mrsValidees
}