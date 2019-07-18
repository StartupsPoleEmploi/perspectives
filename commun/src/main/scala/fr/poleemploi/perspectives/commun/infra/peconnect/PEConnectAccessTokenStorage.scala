package fr.poleemploi.perspectives.commun.infra.peconnect

import fr.poleemploi.perspectives.authentification.infra.peconnect.ws.AccessTokenResponse
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.infra.ws.AccessToken
import play.api.cache.AsyncCacheApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectAccessTokenStorage(asyncCacheApi: AsyncCacheApi) {

  def add(candidatId: CandidatId, accessTokenResponse: AccessTokenResponse): Future[Unit] =
    asyncCacheApi.set(
      key = candidatId.value,
      value = accessTokenResponse.accessToken,
      expiration = accessTokenResponse.expiresIn
    ).map(_ => ())

  def find(candidatId: CandidatId): Future[Option[AccessToken]] =
    asyncCacheApi.get[AccessToken](candidatId.value)

  def remove(candidatId: CandidatId): Future[Unit] =
    asyncCacheApi.remove(candidatId.value).map(_ => ())

}
