package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import akka.util.Timeout
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSPEConnect.{mergeAndRemoveDuplicate, toMRSValidee}
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectAccessTokenStorage, PEConnectId}
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.{AccessToken, PEConnectWSAdapter}
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ReferentielMRSPEConnect(peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                              peConnectSqlAdapter: PEConnectSqlAdapter,
                              peConnectWSAdapter: PEConnectWSAdapter,
                              mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter) extends ReferentielMRS with WSAdapter {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      accessToken <- getCandidatAccessToken(candidatId, candidatPEConnect.peConnectId)
      (mrsValidees, dhaeValidees) <- peConnectWSAdapter.mrsValideesCandidat(accessToken) zip getCandidatDHAEList(candidatPEConnect)
    } yield mergeAndRemoveDuplicate(mrsValidees, dhaeValidees)

  private def getCandidatAccessToken(candidatId: CandidatId, peConnectId: PEConnectId): Future[AccessToken] =
    peConnectAccessTokenStorage.find(peConnectId).flatMap {
      case None => Future.failed(new IllegalArgumentException(s"Aucun token stocké pour le candidat ${candidatId.value}"))
      case Some(accessToken) => peConnectAccessTokenStorage.remove(peConnectId).map(_ => accessToken)
    }

  private def getCandidatDHAEList(c: CandidatPEConnect): Future[Seq[MRSValidee]] =
    mrsDHAEValideesSqlAdapter.findByPeConnectId(c.peConnectId).map(toMRSValidee)


}

object ReferentielMRSPEConnect {
  def toMRSValidee(seq: Seq[MRSDHAEValideePEConnect]): Seq[MRSValidee] =
    seq.map(mrs => MRSValidee(mrs.codeROME, mrs.codeDepartement, mrs.dateEvaluation, isDHAE = true))

  def mergeAndRemoveDuplicate(c1: Seq[MRSValidee], c2: Seq[MRSValidee]): List[MRSValidee] =
    (c1.filterNot(e1 => c2.exists(e2 => e2.codeROME == e1.codeROME && e2.codeDepartement == e1.codeDepartement)) ++ c2).toList
}
