package conf

import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinConfig
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.localisation.infra.algolia.AlgoliaPlacesConfig
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.LocalisationWSAdapterConfig
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.geo.infra.ws.ReferentielRegionWSConfig
import fr.poleemploi.perspectives.commun.infra.Environnement
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.oauth.{EmploiStoreOauthScopeBuilder, OauthConfig}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapterConfig
import fr.poleemploi.perspectives.commun.infra.slack.SlackConfig
import fr.poleemploi.perspectives.conseiller.ConseillerId
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapterConfig
import fr.poleemploi.perspectives.infra.BuildInfo
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapterConfig
import fr.poleemploi.perspectives.offre.infra.ws.ReferentielOffreWSAdapterConfig
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackConfig
import fr.poleemploi.perspectives.projections.recruteur.infra.slack.RecruteurNotificationSlackConfig
import fr.poleemploi.perspectives.rome.infra.ws.ReferentielRomeWSAdapterConfig
import play.api.Configuration

class WebAppConfig(configuration: Configuration) {

  val usePEConnect: Boolean = configuration.getOptional[Boolean]("usePEConnect").getOrElse(true)
  val useSlackNotification: Boolean = configuration.getOptional[Boolean]("useSlackNotification").getOrElse(true)
  val useMailjet: Boolean = configuration.getOptional[Boolean]("useMailjet").getOrElse(true)
  val useGoogleTagManager: Boolean = configuration.getOptional[Boolean]("useGoogleTagManager").getOrElse(true)
  val useReferentielMetier: Boolean = configuration.getOptional[Boolean]("useReferentielMetier").getOrElse(true)
  val useReferentielRome: Boolean = configuration.getOptional[Boolean]("useReferentielRome").getOrElse(true)
  val useReferentielProspectCandidat: Boolean = configuration.getOptional[Boolean]("useReferentielProspectCandidat").getOrElse(true)
  val useReferentielOffre: Boolean = configuration.getOptional[Boolean]("useReferentielOffre").getOrElse(true)
  val useReferentielHabiletesMRS: Boolean = configuration.getOptional[Boolean]("useReferentielHabiletesMRS").getOrElse(true)
  val useLocalisation: Boolean = configuration.getOptional[Boolean]("useLocalisation").getOrElse(true)
  val useReferentielRegion: Boolean = configuration.getOptional[Boolean]("useReferentielRegion").getOrElse(true)

  val environnement: Environnement = Environnement.from(configuration.get[String]("environnement"))
  val version: String = BuildInfo.version

  val adminApiKey: String = configuration.get[String]("admin.apiKey")

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
  val candidatsPEConnectTesteurs: List[PEConnectId] =
    configuration.getOptional[Seq[String]]("emploiStore.candidat.testeurs")
      .map(_.map(PEConnectId).toList)
      .getOrElse(Nil)

  val recruteurOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "employeur",
    scopes = emploiStoreOauthScopeBuilder.avecApiEntreprise.build
  )
  val recruteursPEConnectTesteurs: List[PEConnectId] =
    configuration.getOptional[Seq[String]]("emploiStore.recruteur.testeurs")
      .map(_.map(PEConnectId).toList)
      .getOrElse(Nil)

  val partenaireOauthConfig: OauthConfig = OauthConfig(
    clientId = configuration.get[String]("emploiStore.oauth2.clientId"),
    clientSecret = configuration.get[String]("emploiStore.oauth2.clientSecret"),
    urlAuthentification = configuration.get[String]("emploiStore.entreprise.urlAuthentification"),
    realm = "partenaire",
    scopes = emploiStoreOauthScopeBuilder.avecApiOffre.avecApiRome.build
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

  val localisationWSAdapterConfig: LocalisationWSAdapterConfig = LocalisationWSAdapterConfig(
    urlApi = configuration.get[String]("localisation.urlApi")
  )

  val referentielMetierWSAdapterConfig: ReferentielMetierWSAdapterConfig = ReferentielMetierWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val referentielOffreWSAdapterConfig: ReferentielOffreWSAdapterConfig = ReferentielOffreWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val referentielRomeWSAdapterConfig: ReferentielRomeWSAdapterConfig = ReferentielRomeWSAdapterConfig(
    urlApi = configuration.get[String]("emploiStore.urlApi"),
    oauthConfig = partenaireOauthConfig
  )

  val referentielRegionWSConfig: ReferentielRegionWSConfig = ReferentielRegionWSConfig(
    urlApi = configuration.get[String]("referentielRegion.urlApi"),
  )

  val esConfig: EsConfig = EsConfig(
    host = configuration.get[String]("elasticsearch.host"),
    port = configuration.get[Int]("elasticsearch.port")
  )

  val algoliaPlacesConfig: AlgoliaPlacesConfig = AlgoliaPlacesConfig(
    appId = configuration.get[String]("algoliaPlaces.appId"),
    apiKey = configuration.get[String]("algoliaPlaces.apiKey")
  )

  val autologinConfig: AutologinConfig = AutologinConfig(
    secretKey = configuration.get[String]("autologin.secretKey"),
    issuer = configuration.get[String]("autologin.issuer"),
    expirationInSeconds = configuration.get[Long]("autologin.expirationInSeconds")
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
