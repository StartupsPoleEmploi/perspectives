package conf

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.localisation.infra.algolia.AlgoliaPlacesConfig
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.LocalisationWSAdapterConfig
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.oauth.{EmploiStoreOauthScopeBuilder, OauthConfig}
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapterConfig
import fr.poleemploi.perspectives.commun.infra.slack.SlackConfig
import fr.poleemploi.perspectives.conseiller.ConseillerId
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import fr.poleemploi.perspectives.offre.infra.ws.ReferentielOffreWSAdapterConfig
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackConfig
import fr.poleemploi.perspectives.projections.recruteur.infra.slack.RecruteurNotificationSlackConfig
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

  val emploiStoreOauthScopeBuilder = new EmploiStoreOauthScopeBuilder(environnement)
  val candidatOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.candidat.urlAuthentification"),
    realm = "individu",
    scopes = emploiStoreOauthScopeBuilder
        .avecApiIndividu
        .avecApiCoordonnees
        .avecApiStatut
        .avecApiPrestations
        .avecApiCompetences
        .avecApiFormations
        .avecApiExperiencesProfessionnelles
        .build
  )

  val recruteurOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "employeur",
    scopes = emploiStoreOauthScopeBuilder.avecApiEntreprise.build
  )

  val partenaireOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "partenaire",
    scopes = emploiStoreOauthScopeBuilder.avecApiOffre.build
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

  val recruteurNotificationSlackConfig: RecruteurNotificationSlackConfig = RecruteurNotificationSlackConfig(
    slackConfig = slackConfig,
    environnement = environnement
  )

  val mailjetWSAdapterConfig: MailjetWSAdapterConfig = MailjetWSAdapterConfig(
    urlApi = configuration.get[String]("mailjet.urlApi"),
    senderAdress = Email(configuration.get[String]("mailjet.sender")),
    apiKeyPublic = configuration.get[String]("mailjet.apiKey.public"),
    apiKeyPrivate = configuration.get[String]("mailjet.apiKey.private")
  )
  val mailjetTesteurs: List[Email] =
    configuration.getOptional[Seq[String]]("mailjet.testeurs").map(_.map(Email).toList).getOrElse(Nil)

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

  val candidatsConseillers: Map[CandidatId, ConseillerId] =
    configuration.getOptional[Map[String, String]]("conseillers.candidats")
      .map(_.map(v => (CandidatId(v._1), ConseillerId(v._2))))
      .getOrElse(Map())

  val conseillersAdmins: List[ConseillerId] =
    configuration.getOptional[Seq[String]]("conseillers.admins")
      .map(_.map(ConseillerId).toList)
      .getOrElse(Nil)
}
