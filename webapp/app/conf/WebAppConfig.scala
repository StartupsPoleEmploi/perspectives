package conf

import fr.poleemploi.perspectives.authentification.infra.peconnect.ws.PEConnectWSAdapterConfig
import fr.poleemploi.perspectives.candidat.localisation.infra.algolia.AlgoliaPlacesConfig
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.LocalisationWSAdapterConfig
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.oauth.OauthConfig
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import fr.poleemploi.perspectives.offre.infra.ws.ReferentielOffreWSAdapterConfig
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackConfig
import fr.poleemploi.perspectives.recruteur.commentaire.infra.slack.CommentaireSlackConfig
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

  val candidatNotificationSlackConfig: CandidatNotificationSlackConfig = CandidatNotificationSlackConfig(
    webhookURL = configuration.get[String]("slack.webhook.url"),
    environnement = environnement
  )

  val commentaireSlackConfig: CommentaireSlackConfig = CommentaireSlackConfig(
    webhookURL = configuration.get[String]("slack.webhook.url")
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
    oauthConfig = partenaireOauthConfig,
    scopes = List("api_offresdemploiv2 o2dsoffre") ++
      (if (Environnement.PRODUCTION == environnement)
        List("qos_silver_offresdemploiv2")
      else Nil)
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
