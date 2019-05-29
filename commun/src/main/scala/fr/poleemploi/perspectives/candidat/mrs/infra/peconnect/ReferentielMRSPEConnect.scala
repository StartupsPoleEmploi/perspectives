package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSPEConnect.prioriserMRSDHAEValidees
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.{AccessToken, PEConnectWSAdapter}
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectAccessTokenStorage}
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMRSPEConnect(peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                              peConnectSqlAdapter: PEConnectSqlAdapter,
                              peConnectWSAdapter: PEConnectWSAdapter,
                              mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter) extends ReferentielMRS with WSAdapter {

  override def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      accessToken <- getCandidatAccessToken(candidatPEConnect)
      (mrsValidees, mrsDHAEValidees) <-
        peConnectWSAdapter.mrsValideesCandidat(accessToken) zip mrsDHAEValideesSqlAdapter.findByPeConnectId(candidatPEConnect.peConnectId)
    } yield prioriserMRSDHAEValidees(mrsValidees, mrsDHAEValidees)

  private def getCandidatAccessToken(candidat: CandidatPEConnect): Future[AccessToken] =
    peConnectAccessTokenStorage.find(candidat.peConnectId).flatMap {
      case None => Future.failed(new IllegalArgumentException(s"Aucun token stocké pour le candidat ${candidat.candidatId.value}"))
      case Some(accessToken) => peConnectAccessTokenStorage.remove(candidat.peConnectId).map(_ => accessToken)
    }
}

object ReferentielMRSPEConnect {

  def prioriserMRSDHAEValidees(mrsValidees: List[MRSValidee], mrsDHAEValidees: List[MRSValidee]): List[MRSValidee] =
    mrsValidees.filterNot(e1 => mrsDHAEValidees.exists(e2 => e2.codeROME == e1.codeROME && e2.codeDepartement == e1.codeDepartement)) ++ mrsDHAEValidees
}
