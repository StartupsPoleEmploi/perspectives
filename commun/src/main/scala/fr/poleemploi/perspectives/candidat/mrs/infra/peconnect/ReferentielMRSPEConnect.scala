package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import akka.util.Timeout
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRS}
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectAccessTokenStorage
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapter
import fr.poleemploi.perspectives.commun.infra.slack.SlackConfig
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ReferentielMRSPEConnect(mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                              peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                              peConnectSqlAdapter: PEConnectSqlAdapter,
                              peConnectWSAdapter: PEConnectWSAdapter,
                              wsClient: WSClient,
                              slackConfig: SlackConfig,
                              environnement: Environnement) extends ReferentielMRS with WSAdapter {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def mrsValidees(candidatId: CandidatId): Future[List[MRSValidee]] =
    for {
      candidatPEConnect <- peConnectSqlAdapter.getCandidat(candidatId)
      mrsValidees <- mrsValideesSqlAdapter.mrsValideesParCandidat(candidatPEConnect.peConnectId)
      _ <-
        (for {
          optAccessToken <- peConnectAccessTokenStorage.find(candidatPEConnect.peConnectId)
          accessToken <- optAccessToken
            .map(a => peConnectAccessTokenStorage.remove(candidatPEConnect.peConnectId).map(_ => a))
            .getOrElse(Future.failed(new IllegalArgumentException(s"Aucun token stocké pour le candidat ${candidatId.value}")))
          mrsValideesApi <- peConnectWSAdapter.mrsValideesCandidat(accessToken)
          _ <-
            if (environnement != Environnement.DEVELOPPEMENT)
              wsClient
                .url(s"${slackConfig.webhookURL}")
                .addHttpHeaders(jsonContentType)
                .post(Json.obj("text" ->
                  s"""
                     |Candidat ${candidatId.value} en ${environnement.value}
                     |MRS de l'API prestations :
                     |${buildMRSValidees(mrsValideesApi)}
                     |MRS du Datalake :
                     |${buildMRSValidees(mrsValidees)}
                 """.stripMargin
                ))
                .flatMap(filtreStatutReponse(_))
                .map(_ => ())
            else Future.successful(())
        } yield ())
          .recover {
            case t: Throwable => peConnectLogger.error(s"Erreur lors de la récupération des MRS via PEConnect", t)
          }
    } yield mrsValidees

  private def buildMRSValidees(mrsValidees: List[MRSValidee]): String =
    if (mrsValidees.nonEmpty) mrsValidees.map(buildMRSValidee).mkString("\n") else "Pas de MRS"

  private def buildMRSValidee(mrs: MRSValidee): String =
    s"CodeROME : ${mrs.codeROME.value}. Département : ${mrs.codeDepartement.value}. Date : ${mrs.dateEvaluation}"

}