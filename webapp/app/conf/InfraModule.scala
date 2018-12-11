package conf

import akka.actor.ActorSystem
import authentification.infra.play.PlayOauthService
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject._
import com.google.inject.name.Named
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore, EventStoreListener}
import fr.poleemploi.eventsourcing.infra.akka.AkkaEventStoreListener
import fr.poleemploi.eventsourcing.infra.jackson.EventStoreObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgreSQLAppendOnlyStore, PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.perspectives.authentification.infra.peconnect.PEConnectAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.jwt.PEConnectJWTAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.authentification.infra.peconnect.ws.PEConnectWSAdapter
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.localisation.infra.local.LocalisationLocalAdapter
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.{LocalisationWSAdapter, LocalisationWSMapping}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ReferentielHabiletesMRSLocal, ReferentielMRSCandidatLocal}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.{MRSValideesCandidatsSqlAdapter, ReferentielMRSCandidatPEConnect}
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.oauth.OauthService
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetWSAdapter, MailjetWSMapping}
import fr.poleemploi.perspectives.metier.infra.file.ReferentielMetierFileAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
import fr.poleemploi.perspectives.recruteur.commentaire.infra.local.CommentaireLocalAdapter
import fr.poleemploi.perspectives.recruteur.commentaire.infra.slack.CommentaireSlackAdapter
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
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
  @Named("eventStoreObjectMapper")
  def eventObjectMapper: ObjectMapper =
    EventStoreObjectMapperBuilder(PerspectivesEventSourcingModule).build()

  @Provides
  @Singleton
  def eventStoreListener(actorSystem: ActorSystem): EventStoreListener =
    new AkkaEventStoreListener(actorSystem = actorSystem)

  @Provides
  def postgreSqlAppendOnlyStore(database: Database,
                                @Named("eventStoreObjectMapper") objectMapper: ObjectMapper): PostgreSQLAppendOnlyStore =
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
  def provideDatabase(lifecycle: ApplicationLifecycle,
                      configuration: Configuration): Database = {
    val database = Database.forConfig(
      path = "db.postgresql",
      config = configuration.underlying
    )

    lifecycle.addStopHook(() => Future(database.close()))

    database
  }

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
  def peConnectWSAdapter(webAppConfig: WebAppConfig,
                         wsClient: WSClient): PEConnectWSAdapter =
    new PEConnectWSAdapter(
      wsClient = wsClient,
      config = webAppConfig.peConnectWSAdapterConfig,
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
  def referentielMetierWSAdapter(wsClient: WSClient,
                                 webAppConfig: WebAppConfig): ReferentielMetierWSAdapter =
    new ReferentielMetierWSAdapter(
      config = webAppConfig.referentielMetierWSAdapterConfig,
      wsClient = wsClient
    )

  @Provides
  def referentielMetierFileAdapter: ReferentielMetierFileAdapter =
    new ReferentielMetierFileAdapter()

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
  def commentaireServiceLocal: CommentaireServiceLocal =
    new CommentaireServiceLocal

  @Provides
  def slackCommentaireAdapter(wsClient: WSClient,
                              webAppConfig: WebAppConfig): SlackCommentaireAdapter =
    new SlackCommentaireAdapter(
      wsClient = wsClient,
      config = webAppConfig.slackRecruteurConfig
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
}
