package fr.poleemploi.perspectives.commun.infra.peconnect

import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.{AccessToken, PEConnectCandidatInfos, PEConnectRecruteurInfos, PEConnectWSAdapter}

import scala.concurrent.Future

class PEConnectAdapter(peConnectWSAdapter: PEConnectWSAdapter,
                       peConnectSqlAdapter: PEConnectSqlAdapter) {

  def findCandidat(peConnectId: PEConnectId): Future[Option[CandidatPEConnect]] =
    peConnectSqlAdapter.findCandidat(peConnectId)

  def saveCandidat(candidatPEConnect: CandidatPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveCandidat(candidatPEConnect)

  def infosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    peConnectWSAdapter.infosCandidat(accessToken)

  def findRecruteur(peConnectId: PEConnectId): Future[Option[RecruteurPEConnect]] =
    peConnectSqlAdapter.findRecruteur(peConnectId)

  def saveRecruteur(recruteurPEConnect: RecruteurPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveRecruteur(recruteurPEConnect)

  def infosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    peConnectWSAdapter.infosRecruteur(accessToken)
}
