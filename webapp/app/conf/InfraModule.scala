package conf

import authentification.infra.peconnect.{OauthService, PEConnectFacade, PEConnectInscrisService, PEConnectWS}
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore}
import fr.poleemploi.eventsourcing.infra.jackson.EventStoreObjectMapper
import fr.poleemploi.eventsourcing.infra.sql.{PostgreSQLAppendOnlyStore, PostgresDriver}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher, LocalEventHandler, LocalEventPublisher}
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
    EventStoreObjectMapper.mapper

  @Provides
  @Singleton
  def eventPublisher: EventPublisher = new LocalEventPublisher

  @Provides
  @Singleton
  def eventHandler: EventHandler = new LocalEventHandler

  @Provides
  @Singleton
  def postgreSqlAppendOnlyStore(database: Database,
                                @Named("eventStoreObjectMapper") objectMapper: ObjectMapper): PostgreSQLAppendOnlyStore =
    new PostgreSQLAppendOnlyStore(
      driver = PostgresDriver,
      database = database,
      objectMapper = objectMapper
    )

  @Provides
  @Singleton
  def appendOnlyStore(postgreSQLAppendOnlyStore: Provider[PostgreSQLAppendOnlyStore]): AppendOnlyStore =
    postgreSQLAppendOnlyStore.get()

  @Provides
  @Singleton
  def eventStore(eventPublisher: EventPublisher,
                 appendOnlyStore: AppendOnlyStore): EventStore =
    new EventStore(
      eventPublisher = eventPublisher,
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
  @Singleton
  def oauthService(tokenProvider: TokenProvider): OauthService =
    new OauthService(tokenProvider = tokenProvider)

  @Provides
  @Singleton
  def peConnectInscrisService(database: Database): PEConnectInscrisService =
    new PEConnectInscrisService(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def peConnectWS(webAppConfig: WebAppConfig,
                  wsClient: WSClient): PEConnectWS =
    new PEConnectWS(
      wsClient = wsClient,
      peConnectRecruteurConfig = webAppConfig.peConnectRecruteurConfig,
      peConnectCandidatConfig = webAppConfig.peConnectCandidatConfig
    )

  @Provides
  @Singleton
  def peConnectFacade(oauthService: OauthService,
                      peConnectWS: PEConnectWS,
                      peConnectInscrisService: PEConnectInscrisService): PEConnectFacade =
    new PEConnectFacade(
      oauthService = oauthService,
      peConnectWS = peConnectWS,
      peConnectInscrisService = peConnectInscrisService
    )
}
