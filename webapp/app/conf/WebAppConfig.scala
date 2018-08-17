package conf

import authentification.infra.peconnect.{OAuthConfig, PEConnectCandidatConfig, PEConnectRecruteurConfig}
import fr.poleemploi.perspectives.infra.{BuildInfo, Environnement}
import fr.poleemploi.perspectives.projections.candidat.SlackCandidatConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotificationCandidat: Boolean = configuration.getOptional[Boolean]("useSlackNotificationCandidat").getOrElse(true)
  val useGoogleTagManager: Boolean = configuration.getOptional[Boolean]("useGoogleTagManager").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))
  val version: String = BuildInfo.version

  val oauthConfig = OAuthConfig(
    clientId = configuration.get[String]("peconnect.oauth2.clientId"),
    clientSecret = configuration.get[String]("peconnect.oauth2.clientSecret")
  )

  val peConnectRecruteurConfig: PEConnectRecruteurConfig = PEConnectRecruteurConfig(
    urlAuthentification = configuration.get[String]("peconnect.recruteur.urlAuthentification"),
    urlApi = configuration.get[String]("peconnect.recruteur.urlApi"),
    oauthConfig = oauthConfig,
  )

  val peConnectCandidatConfig: PEConnectCandidatConfig = PEConnectCandidatConfig(
    urlAuthentification = configuration.get[String]("peconnect.candidat.urlAuthentification"),
    urlApi = configuration.get[String]("peconnect.candidat.urlApi"),
    oauthConfig = oauthConfig
  )

  val googleTagManagerContainerId: String = configuration.get[String]("googleTagManager.containerId")

  val slackCandidatConfig = SlackCandidatConfig(
    webhookURL = configuration.get[String]("slack.notificationInscriptionCandidat.url"),
    environnement = environnement
  )

  val admins: List[String] = configuration.getOptional[Seq[String]]("admins").map(_.toList).getOrElse(Nil)
  
  val candidatsTesteurs: List[String] = configuration.getOptional[Seq[String]]("candidatsTesteurs").map(_.toList).getOrElse(Nil)
}
