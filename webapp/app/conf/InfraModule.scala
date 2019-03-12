package conf

import akka.actor.ActorSystem
import authentification.infra.play.PlayOauthService
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject._
import com.google.inject.name.Named
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore, EventStoreListener}
import fr.poleemploi.eventsourcing.infra.akka.AkkaEventStoreListener
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgreSQLAppendOnlyStore, PostgreSQLSnapshotStore, PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.PEConnectJWTAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.ws.{PEConnectWSAdapter, PEConnectWSMapping}
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.localisation.infra.local.LocalisationLocalAdapter
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.{LocalisationWSAdapter, LocalisationWSMapping}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ReferentielHabiletesMRSLocal, ReferentielMRSCandidatLocal}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.{MRSValideesCandidatsSqlAdapter, ReferentielMRSCandidatPEConnect}
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.oauth.OauthService
import fr.poleemploi.perspectives.commun.infra.play.cache.InMemoryCacheApi
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetWSAdapter, MailjetWSMapping}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.ReferentielMetierImpl
import fr.poleemploi.perspectives.metier.infra.elasticsearch.ReferentielMetierElasticsearchAdapter
import fr.poleemploi.perspectives.metier.infra.local.ReferentielMetierLocalAdapter
import fr.poleemploi.perspectives.metier.infra.ws.{ReferentielMetierWSAdapter, ReferentielMetierWSMapping}
import fr.poleemploi.perspectives.offre.infra.local.ReferentielOffreLocalAdapter
import fr.poleemploi.perspectives.offre.infra.ws.{ReferentielOffreWSAdapter, ReferentielOffreWSMapping}
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.{CandidatProjectionElasticsearchAdapter, CandidatProjectionElasticsearchMapping}
import fr.poleemploi.perspectives.projections.candidat.infra.local.CandidatNotificationLocalAdapter
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackAdapter
import fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql.AlerteRecruteurSqlAdapter
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
import fr.poleemploi.perspectives.recruteur.commentaire.infra.local.CommentaireLocalAdapter
import fr.poleemploi.perspectives.recruteur.commentaire.infra.slack.CommentaireSlackAdapter
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import play.filters.csrf.CSRF.TokenProvider
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InfraModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def webappConfig(configuration: Configuration): WebAppConfig =
    new WebAppConfig(configuration = configuration)

  @Provides
  @Singleton
  @Named("eventSourcingObjectMapper")
  def eventSourcingObjectMapper: ObjectMapper =
    EventSourcingObjectMapperBuilder(PerspectivesEventSourcingModule).build()

  @Provides
  @Singleton
  def eventStoreListener(actorSystem: ActorSystem): EventStoreListener =
    new AkkaEventStoreListener(actorSystem = actorSystem)

  @Provides
  def postgreSqlAppendOnlyStore(database: Database,
                                @Named("eventSourcingObjectMapper") objectMapper: ObjectMapper): PostgreSQLAppendOnlyStore =
    new PostgreSQLAppendOnlyStore(
      driver = EventSourcingPostgresDriver,
      database = database,
      objectMapper = objectMapper
    )

  @Provides
  @Singleton
  def appendOnlyStore(postgreSQLAppendOnlyStore: Provider[PostgreSQLAppendOnlyStore]): AppendOnlyStore =
    postgreSQLAppendOnlyStore.get()

  @Provides
  @Singleton
  def eventStore(eventStoreListener: EventStoreListener,
                 appendOnlyStore: AppendOnlyStore): EventStore =
    new EventStore(
      eventStoreListener = eventStoreListener,
      appendOnlyStore = appendOnlyStore
    )

  @Provides
  @Singleton
  def postgreSQLSnapshotStore(database: Database): PostgreSQLSnapshotStore =
    new PostgreSQLSnapshotStore(
      driver = EventSourcingPostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def snapshotStore(postgreSQLSnapshotStore: Provider[PostgreSQLSnapshotStore]): SnapshotStore =
    postgreSQLSnapshotStore.get()

  @Provides
  @Singleton
  def database(lifecycle: ApplicationLifecycle,
               configuration: Configuration): Database = {
    val database = Database.forConfig(
      path = "db.postgresql",
      config = configuration.underlying
    )

    lifecycle.addStopHook(() => Future(database.close()))

    database
  }

  @Provides
  def asyncCacheApi: AsyncCacheApi = new InMemoryCacheApi

  @Provides
  def oauthService(tokenProvider: TokenProvider): OauthService =
    new PlayOauthService(tokenProvider = tokenProvider)

  @Provides
  def peConnectSqlAdapter(database: Database): PEConnectSqlAdapter =
    new PEConnectSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def peConnectJWTAdapter(webAppConfig: WebAppConfig): PEConnectJWTAdapter =
    new PEConnectJWTAdapter(
      recruteurOauthConfig = webAppConfig.recruteurOauthConfig,
      candidatOauthConfig = webAppConfig.candidatOauthConfig
    )

  @Provides
  def peConnectWSWSMapping: PEConnectWSMapping =
    new PEConnectWSMapping()

  @Provides
  def peConnectWSAdapter(webAppConfig: WebAppConfig,
                         mapping: PEConnectWSMapping,
                         wsClient: WSClient): PEConnectWSAdapter =
    new PEConnectWSAdapter(
      wsClient = wsClient,
      config = webAppConfig.peConnectWSAdapterConfig,
      mapping = mapping,
      recruteurOauthConfig = webAppConfig.recruteurOauthConfig,
      candidatOauthConfig = webAppConfig.candidatOauthConfig
    )

  @Provides
  @Singleton
  def peConnectService(oauthService: OauthService,
                       peConnectWSAdapter: PEConnectWSAdapter,
                       peConnectSqlAdapter: PEConnectSqlAdapter,
                       peConnectJWTAdapter: PEConnectJWTAdapter): PEConnectAdapter =
    new PEConnectAdapter(
      oauthService = oauthService,
      peConnectWSAdapter = peConnectWSAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter,
      peConnectJWTAdapter = peConnectJWTAdapter
    )

  @Provides
  def csvSqlAdapter(database: Database): CVSqlAdapter =
    new CVSqlAdapter(
      database = database,
      driver = PostgresDriver
    )

  @Provides
  def mrsValideesCandidatsSqlAdapter(database: Database): MRSValideesCandidatsSqlAdapter =
    new MRSValideesCandidatsSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def mailjetSqlAdapter(database: Database): MailjetSqlAdapter =
    new MailjetSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def mailjetWSAdapter(wsClient: WSClient,
                       webAppConfig: WebAppConfig,
                       mailjetWSMapping: MailjetWSMapping): MailjetWSAdapter =
    new MailjetWSAdapter(
      wsClient = wsClient,
      config = webAppConfig.mailjetWSAdapterConfig,
      mailjetWSMapping = mailjetWSMapping
    )

  @Provides
  def mailjetWSMapping: MailjetWSMapping =
    new MailjetWSMapping()

  @Provides
  def mailjetEmailingService(mailjetSqlAdapter: MailjetSqlAdapter,
                             mailjetWSAdapter: MailjetWSAdapter): MailjetEmailingService =
    new MailjetEmailingService(
      mailjetSqlAdapter = mailjetSqlAdapter,
      mailjetWSAdapter = mailjetWSAdapter
    )

  @Provides
  def localEmailingService: LocalEmailingService =
    new LocalEmailingService

  @Provides
  def referentielMetierWSMapping: ReferentielMetierWSMapping =
    new ReferentielMetierWSMapping()

  @Provides
  def referentielMetierWSAdapter(wsClient: WSClient,
                                 mapping: ReferentielMetierWSMapping,
                                 webAppConfig: WebAppConfig,
                                 cacheApi: AsyncCacheApi): ReferentielMetierWSAdapter =
    new ReferentielMetierWSAdapter(
      config = webAppConfig.referentielMetierWSAdapterConfig,
      mapping = mapping,
      wsClient = wsClient,
      cacheApi = cacheApi
    )

  @Provides
  def referentielMetierElasticsearchAdapter(wsClient: WSClient,
                                            webAppConfig: WebAppConfig): ReferentielMetierElasticsearchAdapter =
    new ReferentielMetierElasticsearchAdapter(
      esConfig = webAppConfig.esConfig,
      wsClient = wsClient
    )

  @Provides
  def referentielMetierImpl(referentielMetierWSAdapter: ReferentielMetierWSAdapter,
                            referentielMetierElasticsearchAdapter: ReferentielMetierElasticsearchAdapter): ReferentielMetierImpl =
    new ReferentielMetierImpl(
      wsAdapter = referentielMetierWSAdapter,
      elasticsearchAdapter = referentielMetierElasticsearchAdapter
    )

  @Provides
  def referentielMetierLocalAdapter: ReferentielMetierLocalAdapter =
    new ReferentielMetierLocalAdapter()

  @Provides
  def referentielMRSCandidatPEConnect(mrsValideesCandidatsSqlAdapter: MRSValideesCandidatsSqlAdapter,
                                      peConnectSqlAdapter: PEConnectSqlAdapter): ReferentielMRSCandidatPEConnect =
    new ReferentielMRSCandidatPEConnect(
      mrsValideesCandidatsSqlAdapter = mrsValideesCandidatsSqlAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter
    )

  @Provides
  def referentielMRSCandidatLocal: ReferentielMRSCandidatLocal =
    new ReferentielMRSCandidatLocal

  @Provides
  def referentielHabiletesMRSSqlAdapter(database: Database): ReferentielHabiletesMRSSqlAdapter =
    new ReferentielHabiletesMRSSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def referentielHabiletesMRSLocal: ReferentielHabiletesMRSLocal =
    new ReferentielHabiletesMRSLocal()

  @Provides
  def commentaireLocalAdapter: CommentaireLocalAdapter =
    new CommentaireLocalAdapter

  @Provides
  def commentaireSlackAdapter(wsClient: WSClient,
                              webAppConfig: WebAppConfig): CommentaireSlackAdapter =
    new CommentaireSlackAdapter(
      wsClient = wsClient,
      config = webAppConfig.commentaireSlackConfig
    )

  @Provides
  def localisationLocalAdapter: LocalisationLocalAdapter =
    new LocalisationLocalAdapter

  @Provides
  def localisationWSMapping: LocalisationWSMapping =
    new LocalisationWSMapping()

  @Provides
  def localisationWSAdapter(wsClient: WSClient,
                            mapping: LocalisationWSMapping,
                            webAppConfig: WebAppConfig): LocalisationWSAdapter =
    new LocalisationWSAdapter(
      wsClient = wsClient,
      config = webAppConfig.localisationWSAdapterConfig,
      mapping = mapping
    )

  @Provides
  def candidatNotificationSlackAdapter(webAppConfig: WebAppConfig,
                                       wsClient: WSClient): CandidatNotificationSlackAdapter =
    new CandidatNotificationSlackAdapter(
      config = webAppConfig.candidatNotificationSlackConfig,
      wsClient = wsClient
    )

  @Provides
  def candidatNotificationLocalAdapter: CandidatNotificationLocalAdapter =
    new CandidatNotificationLocalAdapter()

  @Provides
  def referentielOffreWSMapping: ReferentielOffreWSMapping =
    new ReferentielOffreWSMapping()

  @Provides
  def referentielOffreWSAdapter(wsClient: WSClient,
                                webAppConfig: WebAppConfig,
                                referentielOffreWSMapping: ReferentielOffreWSMapping,
                                cacheApi: AsyncCacheApi): ReferentielOffreWSAdapter =
    new ReferentielOffreWSAdapter(
      config = webAppConfig.referentielOffreWSAdapterConfig,
      wsClient = wsClient,
      mapping = referentielOffreWSMapping,
      cacheApi = cacheApi
    )

  @Provides
  def referentielOffreLocalAdapter: ReferentielOffreLocalAdapter =
    new ReferentielOffreLocalAdapter()

  @Provides
  def candidatProjectionElasticsearchMapping(referentielMetier: ReferentielMetier): CandidatProjectionElasticsearchMapping =
    new CandidatProjectionElasticsearchMapping(
      referentielMetier = referentielMetier
    )

  @Provides
  def candidatProjectionElasticsearchAdapter(webAppConfig: WebAppConfig,
                                             wsClient: WSClient,
                                             mapping: CandidatProjectionElasticsearchMapping): CandidatProjectionElasticsearchAdapter =
    new CandidatProjectionElasticsearchAdapter(
      wsClient = wsClient,
      esConfig = webAppConfig.esConfig,
      mapping = mapping
    )

  @Provides
  def recruteurProjectionSqlAdapter(database: Database): RecruteurProjectionSqlAdapter =
    new RecruteurProjectionSqlAdapter(
      database = database
    )

  @Provides
  def alerteRecruteurSqlAdapter(database: Database): AlerteRecruteurSqlAdapter =
    new AlerteRecruteurSqlAdapter(
      database = database
    )
}
