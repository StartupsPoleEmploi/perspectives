package fr.poleemploi.perspectives.commun.infra.peconnect

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.{AccessToken, PEConnectCandidatInfos, PEConnectRecruteurInfos, PEConnectWSAdapter}

import scala.concurrent.Future

class PEConnectAdapter(peConnectWSAdapter: PEConnectWSAdapter,
                       peConnectSqlAdapter: PEConnectSqlAdapter) {

  def findCandidat(peConnectId: PEConnectId): Future[Option[CandidatPEConnect]] =
    peConnectSqlAdapter.findCandidat(peConnectId)

  def saveCandidat(candidatPEConnect: CandidatPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveCandidat(candidatPEConnect)

  def getInfosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    peConnectWSAdapter.getInfosCandidat(accessToken)

  def getPrestationsCandidat(accessToken: AccessToken): Future[List[MRSValidee]] =
    peConnectWSAdapter.getPrestationsCandidat(accessToken)

  def getAdresseCandidat(accessToken: AccessToken): Future[Adresse] =
    peConnectWSAdapter.getCoordonneesCandidat(accessToken)

  def getStatutDemandeurEmploiCandidat(accessToken: AccessToken): Future[StatutDemandeurEmploi] =
    peConnectWSAdapter.getStatutDemandeurEmploiCandidat(accessToken)

  def findRecruteur(peConnectId: PEConnectId): Future[Option[RecruteurPEConnect]] =
    peConnectSqlAdapter.findRecruteur(peConnectId)

  def saveRecruteur(recruteurPEConnect: RecruteurPEConnect): Future[Unit] =
    peConnectSqlAdapter.saveRecruteur(recruteurPEConnect)

  def getInfosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    peConnectWSAdapter.getInfosRecruteur(accessToken)
}
