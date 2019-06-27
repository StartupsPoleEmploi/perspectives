package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.infra.ws.{WSAdapter, WebServiceException}
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectWSAdapter(wsClient: WSClient,
                         config: PEConnectWSAdapterConfig,
                         mapping: PEConnectWSMapping) extends WSAdapter {

  def mrsValideesCandidat(accessToken: AccessToken): Future[List[MRSValidee]] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/prestationssuivies/v1/resultat/rendez-vous?listeCodeTypePrestation=ESP&listeCodeTypePrestation=ESPR")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildMRSValidees(r.json.as[List[ResultatRendezVousResponse]]))

  def infosRecruteur(accessToken: AccessToken): Future[PEConnectRecruteurInfos] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-entreprise/v1/userinfo")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildPEConnectRecruteurInfos(r.json.as[UserInfosEntrepriseResponse]))

  def infosCandidat(accessToken: AccessToken): Future[PEConnectCandidatInfos] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-individu/v1/userinfo")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildPEConnectCandidatInfos(r.json.as[UserInfosResponse]))

  def coordonneesCandidat(accessToken: AccessToken): Future[Adresse] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-coordonnees/v1/coordonnees")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildAdresse(r.json.as[CoordonneesCandidatReponse]))

  def statutDemandeurEmploiCandidat(accessToken: AccessToken): Future[StatutDemandeurEmploi] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-statut/v1/statut")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildStatutDemandeurEmploi(r.json.as[StatutCandidatReponse]))

  def competencesCandidat(accessToken: AccessToken): Future[(List[SavoirEtre], List[SavoirFaire])] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-competences/v2/competences")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => {
      val competences = r.json.as[List[CompetenceResponse]]
      (mapping.buildSavoirEtreProfessionnels(competences), mapping.buildSavoirFaire(competences))
    })

  def languesCandidat(accessToken: AccessToken): Future[List[Langue]] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-competences/v2/langues")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildLanguesCandidat(r.json.as[List[LangueResponse]]))

  def centresInteretCandidat(accessToken: AccessToken): Future[List[CentreInteret]] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-competences/v2/interets")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildCentreInteretsCandidat(r.json.as[List[CentreInteretResponse]]))

  def formationsCandidat(accessToken: AccessToken): Future[List[Formation]] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-formations/v1/formations")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildFormations(r.json.as[List[FormationResponse]]))

  def permisCandidat(accessToken: AccessToken): Future[List[Permis]] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-formations/v1/permis")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildPermis(r.json.as[List[PermisResponse]]))

  def experiencesProfessionnellesCandidat(accessToken: AccessToken): Future[List[ExperienceProfessionnelle]] =
    handleRateLimit()(
      wsClient
        .url(s"${config.urlApi}/peconnect-experiences/v1/experiences")
        .addHttpHeaders(authorizationHeader(accessToken))
        .get()
        .flatMap(filtreStatutReponse(_))
    ).map(r => mapping.buildExperienceProfessionnelles(r.json.as[List[ExperienceProfessionnelleResponse]]))

  private def authorizationHeader(accessToken: AccessToken): (String, String) = ("Authorization", s"Bearer ${accessToken.value}")

  /**
    * Effectue un nombre de réessai si un service échoue en raison d'un statut 429 (Too many requests). <br />
    * Les services de l'emploi store sont limités en nombre de requêtes/seconde,
    * et il se peut qu'un autre client ou nous-mêmes fassions trop de requêtes et bloquions une API pour tous les autres.
    */
  private def handleRateLimit(nbRetriesMax: Int = 5, sleepingTime: Long = 1000)(block: => Future[WSResponse]): Future[WSResponse] =
    if (nbRetriesMax == 0)
      Future.failed(new RuntimeException("Nombre de réessai maximum atteint"))
    else
      block recoverWith {
        case t: WebServiceException if t.statut == 429 =>
          peConnectWSLogger.warn(s"Réessai d'appel suite à un statut ${t.statut}", t)
          handleRateLimit(nbRetriesMax - 1, sleepingTime) {
            Thread.sleep(sleepingTime)
            block
          }
      }
}