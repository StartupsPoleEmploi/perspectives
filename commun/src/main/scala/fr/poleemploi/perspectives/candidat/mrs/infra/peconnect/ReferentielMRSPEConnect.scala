package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSPEConnect.prioriserMRSDHAEValidees
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectAccessTokenStorage
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapter
import fr.poleemploi.perspectives.commun.infra.ws.{AccessToken, WSAdapter}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielMRSPEConnect(peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                              peConnectSqlAdapter: PEConnectSqlAdapter,
                              peConnectWSAdapter: PEConnectWSAdapter,
                              mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter) extends ReferentielMRS with WSAdapter {

  override def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      accessToken <- getCandidatAccessToken(candidatPEConnect.candidatId)
      (mrsValidees, mrsDHAEValidees) <-
        peConnectWSAdapter.mrsValideesCandidat(accessToken) zip mrsDHAEValideesSqlAdapter.findByPeConnectId(candidatPEConnect.peConnectId)
    } yield prioriserMRSDHAEValidees(mrsValidees, mrsDHAEValidees)

  private def getCandidatAccessToken(candidatId: CandidatId): Future[AccessToken] =
    peConnectAccessTokenStorage
      .find(candidatId)
      .map(_.getOrElse(throw new IllegalArgumentException(s"Aucun token stocké pour le candidat ${candidatId.value}")))
}

object ReferentielMRSPEConnect {

  def prioriserMRSDHAEValidees(mrsValidees: List[MRSValidee], mrsDHAEValidees: List[MRSValidee]): List[MRSValidee] =
    mrsValidees.filterNot(e1 => mrsDHAEValidees.exists(e2 => e2.codeROME == e1.codeROME && e2.codeDepartement == e1.codeDepartement)) ++ mrsDHAEValidees
}
