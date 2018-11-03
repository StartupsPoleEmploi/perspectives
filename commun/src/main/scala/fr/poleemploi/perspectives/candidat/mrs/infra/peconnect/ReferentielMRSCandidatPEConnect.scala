package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMRSCandidatPEConnect(mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                                      peConnectSqlAdapter: PEConnectSqlAdapter) extends ReferentielMRSCandidat {

  override def mrsValideesParCandidat(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      mrsValidees <- mrsValideesSqlAdapter.metiersEvaluesParCandidat(candidatPEConnect.peConnectId)
    } yield mrsValidees
}