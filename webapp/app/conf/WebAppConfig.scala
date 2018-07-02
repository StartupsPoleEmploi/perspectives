package conf

import domain.services.PEConnectConfig
import fr.poleemploi.perspectives.infra.Environnement
import fr.poleemploi.perspectives.projections.SlackCandidatConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotificationCandidat: Boolean = configuration.getOptional[Boolean]("useSlackNotificationCandidat").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))

  val peConnectConfig: PEConnectConfig = PEConnectConfig(
    url = configuration.get[String]("peconnect.url"),
    clientId = configuration.get[String]("peconnect.oauth2.clientId"),
    clientSecret = configuration.get[String]("peconnect.oauth2.clientSecret"),
  )

  val peConnectIndividuURL: String = configuration.get[String]("peconnectIndividu.url")

  val googleTagManagerContainerId: String = configuration.get[String]("googleTagManager.containerId")

  val slackCandidatConfig = SlackCandidatConfig(
    webhookURL = configuration.get[String]("slack.notificationInscriptionCandidat.url"),
    environnement = environnement
  )
}
