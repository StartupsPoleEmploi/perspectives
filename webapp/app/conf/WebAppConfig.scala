package conf

import java.nio.file.Paths

import fr.poleemploi.perspectives.authentification.infra.ws.PEConnectWSAdapterConfig
import fr.poleemploi.perspectives.candidat.mrs.infra.ReferentielMRSCandidatConfig
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import fr.poleemploi.perspectives.projections.candidat.SlackCandidatConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotificationCandidat: Boolean = configuration.getOptional[Boolean]("useSlackNotificationCandidat").getOrElse(true)
  val useEmail: Boolean = configuration.getOptional[Boolean]("useEmail").getOrElse(true)
  val useGoogleTagManager: Boolean = configuration.getOptional[Boolean]("useGoogleTagManager").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))
  val version: String = BuildInfo.version

  val oauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("peconnect.oauth2.clientId"),
    clientSecret = configuration.get[String]("peconnect.oauth2.clientSecret")
  )

  val peConnectRecruteurConfig: PEConnectWSAdapterConfig = PEConnectWSAdapterConfig(
    urlAuthentification = configuration.get[String]("peconnect.recruteur.urlAuthentification"),
    urlApi = configuration.get[String]("peconnect.urlApi"),
    oauthConfig = oauthConfig,
  )

  val peConnectCandidatConfig: PEConnectWSAdapterConfig = PEConnectWSAdapterConfig(
    urlAuthentification = configuration.get[String]("peconnect.candidat.urlAuthentification"),
    urlApi = configuration.get[String]("peconnect.urlApi"),
    oauthConfig = oauthConfig
  )

  val googleTagManagerContainerId: String = configuration.get[String]("googleTagManager.containerId")

  val slackCandidatConfig: SlackCandidatConfig = SlackCandidatConfig(
    webhookURL = configuration.get[String]("slack.notificationInscriptionCandidat.url"),
    environnement = environnement
  )

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = configuration.get[String]("mailjet.sender"),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private"),
    testeurs = configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.toList).getOrElse(Nil)
  )

  val referentielMetierWSAdapterConfig: ReferentielMetierWSAdapterConfig = ReferentielMetierWSAdapterConfig(
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
