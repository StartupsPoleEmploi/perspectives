package conf

import java.nio.file.Paths

import authentification.infra.peconnect.{PEConnectCandidatConfig, PEConnectRecruteurConfig}
import fr.poleemploi.perspectives.domain.candidat.mrs.infra.ReferentielMRSCandidatConfig
import fr.poleemploi.perspectives.domain.metier.infra.ReferentielMetierWSConfig
import fr.poleemploi.perspectives.infra.oauth.OAuthConfig
import fr.poleemploi.perspectives.infra.{BuildInfo, Environnement}
import fr.poleemploi.perspectives.projections.candidat.SlackCandidatConfig
import fr.poleemploi.perspectives.projections.infra.MailjetConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotificationCandidat: Boolean = configuration.getOptional[Boolean]("useSlackNotificationCandidat").getOrElse(true)
  val useEmail: Boolean = configuration.getOptional[Boolean]("useEmail").getOrElse(true)
  val useGoogleTagManager: Boolean = configuration.getOptional[Boolean]("useGoogleTagManager").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))
  val version: String = BuildInfo.version

  val oauthConfig: OAuthConfig = OAuthConfig(
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

  val slackCandidatConfig: SlackCandidatConfig = SlackCandidatConfig(
    webhookURL = configuration.get[String]("slack.notificationInscriptionCandidat.url"),
    environnement = environnement
  )

  val mailjetConfig: MailjetConfig = MailjetConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = configuration.get[String]("mailjet.sender"),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private")
  )

  val referentielMetierWSConfig: ReferentielMetierWSConfig = ReferentielMetierWSConfig(
    urlAuthentification = configuration.get[String]("referentielMetier.urlAuthentification"),
    urlApi = configuration.get[String]("referentielMetier.urlApi"),
    oauthConfig = oauthConfig
  )

  val referentielMRSCandidatConfig: ReferentielMRSCandidatConfig = ReferentielMRSCandidatConfig(
    importDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.mrsValidees.importDirectory")),
    archiveDirectory = Paths.get(configuration.get[String]("extractPoleEmploi.mrsValidees.archiveDirectory"))
  )

  val admins: List[String] = configuration.getOptional[Seq[String]]("admins").map(_.toList).getOrElse(Nil)

  val candidatsTesteurs: List[String] = configuration.getOptional[Seq[String]]("candidatsTesteurs").map(_.toList).getOrElse(Nil)
}
