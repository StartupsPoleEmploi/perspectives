package conf

import domain.services.PEConnectConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val peConnectConfig: PEConnectConfig = PEConnectConfig(
    url = configuration.get[String]("peconnect.url"),
    clientId = configuration.get[String]("peconnect.oauth2.clientId"),
    clientSecret = configuration.get[String]("peconnect.oauth2.clientSecret"),
  )

  val peConnectIndividuURL: String = configuration.get[String]("peconnectIndividu.url")

  val googleTagManagerContainerId: String = configuration.get[String]("googleTagManager.containerId")
}
