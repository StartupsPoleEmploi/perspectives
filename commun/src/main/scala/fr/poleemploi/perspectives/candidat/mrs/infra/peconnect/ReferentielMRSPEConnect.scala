package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import akka.util.Timeout
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRS}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectAccessTokenStorage
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapter
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ReferentielMRSPEConnect(mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                              peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                              peConnectSqlAdapter: PEConnectSqlAdapter,
                              peConnectWSAdapter: PEConnectWSAdapter) extends ReferentielMRS with WSAdapter {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      optAccessToken <- peConnectAccessTokenStorage.find(candidatPEConnect.peConnectId)
      accessToken <- optAccessToken
        .map(a => peConnectAccessTokenStorage.remove(candidatPEConnect.peConnectId).map(_ => a))
        .getOrElse(Future.failed(new IllegalArgumentException(s"Aucun token stocké pour le candidat ${candidatId.value}")))
      mrsValidees <- peConnectWSAdapter.mrsValideesCandidat(accessToken)
    } yield mrsValidees
}