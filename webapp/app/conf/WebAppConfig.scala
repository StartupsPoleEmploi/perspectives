package conf

import fr.poleemploi.perspectives.authentification.infra.peconnect.ws.PEConnectWSAdapterConfig
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import fr.poleemploi.perspectives.projections.candidat.SlackCandidatConfig
import fr.poleemploi.perspectives.recruteur.RecruteurId
import fr.poleemploi.perspectives.recruteur.commentaire.infra.slack.SlackRecruteurConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotification: Boolean = configuration.getOptional[Boolean]("useSlackNotification").getOrElse(true)
  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val useGoogleTagManager: Boolean = configuration.getOptional[Boolean]("useGoogleTagManager").getOrElse(true)
  val useReferentielMetierWS: Boolean = configuration.getOptional[Boolean]("useReferentielMetierWS").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))
  val version: String = BuildInfo.version

  val candidatOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.candidat.urlAuthentification"),
    realm = "individu"
  )

  val recruteurOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "employeur"
  )

  val partenaireOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "partenaire"
  )

  val peConnectWSAdapterConfig: PEConnectWSAdapterConfig = PEConnectWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi")
  )

  val googleTagManagerContainerId: String = configuration.get[String]("googleTagManager.containerId")

  val slackCandidatConfig: SlackCandidatConfig = SlackCandidatConfig(
    webhookURL = configuration.get[String]("slack.webhook.url"),
    environnement = environnement
  )

  val slackRecruteurConfig: SlackRecruteurConfig = SlackRecruteurConfig(
    webhookURL = configuration.get[String]("slack.webhook.url")
  )

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = configuration.get[String]("mailjet.sender"),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private"),
    testeurs = configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.toList).getOrElse(Nil)
  )

  val referentielMetierWSAdapterConfig: ReferentielMetierWSAdapterConfig = ReferentielMetierWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val admins: List[String] = configuration.getOptional[Seq[String]]("admins").map(_.toList).getOrElse(Nil)

  val candidatsTesteurs: List[CandidatId] = configuration.getOptional[Seq[String]]("candidatsTesteurs").map(_.toList.map(CandidatId)).getOrElse(Nil)

  val recruteursTesteurs: List[RecruteurId] = configuration.getOptional[Seq[String]]("recruteursTesteurs").map(_.toList.map(RecruteurId)).getOrElse(Nil)
}
