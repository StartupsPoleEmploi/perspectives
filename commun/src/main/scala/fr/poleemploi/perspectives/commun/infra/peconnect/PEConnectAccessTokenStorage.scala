package fr.poleemploi.perspectives.commun.infra.peconnect

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.infra.ws.AccessToken
import play.api.cache.AsyncCacheApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class PEConnectAccessTokenStorage(asyncCacheApi: AsyncCacheApi) {

  def add(candidatId: CandidatId, accessToken: AccessToken): Future[Unit] =
    asyncCacheApi.set(
      key = candidatId.value,
      value = accessToken,
      expiration = 5.minutes
    ).map(_ => ())

  def find(candidatId: CandidatId): Future[Option[AccessToken]] =
    asyncCacheApi.get[AccessToken](candidatId.value)

  def remove(candidatId: CandidatId): Future[Unit] =
    asyncCacheApi.remove(candidatId.value).map(_ => ())

}
