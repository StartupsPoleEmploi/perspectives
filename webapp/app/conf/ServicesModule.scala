package conf

import authentification.{RoleService, SimpleRoleService}
import com.google.inject.{AbstractModule, Provides, Singleton}
import domain.services.{PEConnectIndividuService, PEConnectService}
import play.api.libs.ws.WSClient

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def peConnectService(webAppConfig: WebAppConfig,
                       wsClient: WSClient): PEConnectService =
    new PEConnectService(
      wsClient = wsClient,
      peConnectConfig = webAppConfig.peConnectConfig
    )

  @Provides
  @Singleton
  def peConnectIndividuService(webAppConfig: WebAppConfig,
                               wsClient: WSClient): PEConnectIndividuService =
    new PEConnectIndividuService(
      wsClient = wsClient,
      url = webAppConfig.peConnectIndividuURL
    )

  @Provides
  @Singleton
  def roleService(webAppConfig: WebAppConfig): RoleService = new SimpleRoleService(
    admins = webAppConfig.admins
  )
}
