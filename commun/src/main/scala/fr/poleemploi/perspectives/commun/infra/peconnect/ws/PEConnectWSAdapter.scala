package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.candidat.{Adresse, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectWSAdapter(wsClient: WSClient,
                         config: PEConnectWSAdapterConfig,
                         mapping: PEConnectWSMapping) extends WSAdapter {

  def mrsValideesCandidat(accessToken: AccessToken): Future[List[MRSValidee]] =
    wsClient
      .url(s"${config.urlApi}/prestationssuivies/v1/resultat/rendez-vous?listeCodeTypePrestation=ESP&listeCodeTypePrestation=ESPR")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(_.json.as[List[ResultatRendezVousResponse]].flatMap(mapping.buildMRSValidee))

  def infosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    wsClient
      .url(s"${config.urlApi}/peconnect-entreprise/v1/userinfo")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildPEConnectRecruteurInfos(r.json.as[UserInfosEntrepriseResponse]))

  def infosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    wsClient
      .url(s"${config.urlApi}/peconnect-individu/v1/userinfo")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildPEConnectCandidatInfos(r.json.as[UserInfosResponse]))

  def coordonneesCandidat(accessToken: AccessToken): Future[Adresse] =
    wsClient
      .url(s"${config.urlApi}/peconnect-coordonnees/v1/coordonnees")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildAdresse(r.json.as[CoordonneesCandidatReponse]))

  def satutDemandeurEmploiCandidat(accessToken: AccessToken): Future[StatutDemandeurEmploi] =
    wsClient
      .url(s"${config.urlApi}/peconnect-statut/v1/statut")
      .addHttpHeaders(authorizationHeader(accessToken))
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildStatutDemandeurEmploi(r.json.as[StatutCandidatReponse]))


  private def authorizationHeader(accessToken: AccessToken): (String, String) = ("Authorization", s"Bearer ${accessToken.value}")
}