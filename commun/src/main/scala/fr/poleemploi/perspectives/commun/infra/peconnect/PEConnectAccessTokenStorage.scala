package fr.poleemploi.perspectives.commun.infra.peconnect

import fr.poleemploi.perspectives.commun.infra.peconnect.ws.AccessToken
import play.api.cache.AsyncCacheApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PEConnectAccessTokenStorage(asyncCacheApi: AsyncCacheApi) {

  def add(peConnectId: PEConnectId, accessToken: AccessToken): Future[Unit] =
    asyncCacheApi.set(
      key = peConnectId.value,
      value = accessToken
    ).map(_ => ())

  def find(peConnectId: PEConnectId): Future[Option[AccessToken]] =
    asyncCacheApi.get[AccessToken](peConnectId.value)

  def remove(peConnectId: PEConnectId): Future[Unit] =
    asyncCacheApi.remove(peConnectId.value).map(_ => ())

}
