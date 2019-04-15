package conf

import fr.poleemploi.perspectives.candidat.localisation.infra.algolia.AlgoliaPlacesConfig
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.LocalisationWSAdapterConfig
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.oauth.{OauthConfig, OauthScope}
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapterConfig
import fr.poleemploi.perspectives.commun.infra.slack.SlackConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import fr.poleemploi.perspectives.offre.infra.ws.ReferentielOffreWSAdapterConfig
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotification: Boolean = configuration.getOptional[Boolean]("useSlackNotification").getOrElse(true)
  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val useGoogleTagManager: Boolean = configuration.getOptional[Boolean]("useGoogleTagManager").getOrElse(true)
  val useReferentielMetier: Boolean = configuration.getOptional[Boolean]("useReferentielMetier").getOrElse(true)
  val useReferentielOffre: Boolean = configuration.getOptional[Boolean]("useReferentielOffre").getOrElse(true)
  val useReferentielHabiletesMRS: Boolean = configuration.getOptional[Boolean]("useReferentielHabiletesMRS").getOrElse(true)
  val useLocalisationWS: Boolean = configuration.getOptional[Boolean]("useLocalisationWS").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))
  val version: String = BuildInfo.version

  val candidatOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.candidat.urlAuthentification"),
    realm = "individu",
    scopes = List(OauthScope.API_INDIVIDU, OauthScope.API_COORDONNEES, OauthScope.API_STATUT, OauthScope.API_PRESTATIONS).flatten
  )

  val recruteurOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "employeur",
    scopes = OauthScope.API_ENTREPRISE
  )

  val partenaireOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "partenaire",
    scopes = OauthScope.API_OFFRE ++
      (if (Environnement.PRODUCTION == environnement)
        List(OauthScope.API_OFFRE_QOS_SILVER)
      else Nil)
  )

  val peConnectWSAdapterConfig: PEConnectWSAdapterConfig = PEConnectWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi")
  )

  val googleTagManagerContainerId: String = configuration.get[String]("googleTagManager.containerId")

  val slackConfig: SlackConfig = SlackConfig(configuration.get[String]("slack.webhook.url"))

  val candidatNotificationSlackConfig: CandidatNotificationSlackConfig = CandidatNotificationSlackConfig(
    slackConfig = slackConfig,
    environnement = environnement
  )

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = configuration.get[String]("mailjet.sender"),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private"),
    testeurs = configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.toList).getOrElse(Nil)
  )

  val localisationWSAdapterConfig: LocalisationWSAdapterConfig = LocalisationWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.localisation.urlApi")
  )

  val referentielMetierWSAdapterConfig: ReferentielMetierWSAdapterConfig = ReferentielMetierWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val referentielOffreWSAdapterConfig: ReferentielOffreWSAdapterConfig = ReferentielOffreWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val esConfig: EsConfig = EsConfig(
    host = configuration.get[String]("elasticsearch.host"),
    port = configuration.get[Int]("elasticsearch.port")
  )

  val algoliaPlacesConfig: AlgoliaPlacesConfig = AlgoliaPlacesConfig(
    appId = configuration.get[String]("algoliaPlaces.appId"),
    apiKey = configuration.get[String]("algoliaPlaces.apiKey")
  )

  val admins: List[String] = configuration.getOptional[Seq[String]]("admins").map(_.toList).getOrElse(Nil)
}
