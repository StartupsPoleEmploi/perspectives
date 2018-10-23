package conf

import akka.actor.ActorSystem
import authentification.infra.play.PlayOauthService
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore}
import fr.poleemploi.eventsourcing.infra.jackson.EventStoreObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgreSQLAppendOnlyStore, PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher, LocalEventHandler, LocalEventPublisher}
import fr.poleemploi.perspectives.authentification.infra.PEConnectService
import fr.poleemploi.perspectives.authentification.infra.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.authentification.infra.ws.PEConnectWSAdapter
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.ReferentielMRSCandidatLocal
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSCandidatPEConnect
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.oauth.OauthService
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.metier.infra.file.ReferentielMetierFileAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
import fr.poleemploi.perspectives.recruteur.commentaire.infra.local.CommentaireServiceLocal
import fr.poleemploi.perspectives.recruteur.commentaire.infra.slack.SlackCommentaireAdapter
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
  def eventPublisher: EventPublisher = new LocalEventPublisher

  @Provides
  @Singleton
  def eventHandler: EventHandler = new LocalEventHandler

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
  def oauthService(tokenProvider: TokenProvider): OauthService =
    new PlayOauthService(tokenProvider = tokenProvider)

  @Provides
  def peConnectSqlAdapter(database: Database): PEConnectSqlAdapter =
    new PEConnectSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def peConnectWSAdapter(webAppConfig: WebAppConfig,
                         wsClient: WSClient): PEConnectWSAdapter =
    new PEConnectWSAdapter(
      wsClient = wsClient,
      recruteurConfig = webAppConfig.peConnectRecruteurConfig,
      candidatConfig = webAppConfig.peConnectCandidatConfig
    )

  @Provides
  @Singleton
  def peConnectService(oauthService: OauthService,
                       peConnectWSAdapter: PEConnectWSAdapter,
                       peConnectSqlAdapter: PEConnectSqlAdapter): PEConnectService =
    new PEConnectService(
      oauthService = oauthService,
      peConnectWSAdapter = peConnectWSAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter
    )

  @Provides
  def csvSqlAdapter(database: Database): CVSqlAdapter =
    new CVSqlAdapter(
      database = database,
      driver = PostgresDriver
    )

  @Provides
  def mrsValideesCSVAdapter(actorSystem: ActorSystem): MRSValideesCSVAdapter =
    new MRSValideesCSVAdapter(actorSystem = actorSystem)

  @Provides
  def mrsValideesSqlAdapter(database: Database): MRSValideesSqlAdapter =
    new MRSValideesSqlAdapter(
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
                       webAppConfig: WebAppConfig): MailjetWSAdapter =
    new MailjetWSAdapter(
      wsClient = wsClient,
      config = webAppConfig.mailjetWSAdapterConfig
    )

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
  def referentielMRSCandidatPEConnect(mrsValideesCSVAdapter: MRSValideesCSVAdapter,
                                      mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                                      peConnectService: PEConnectService,
                                      webAppConfig: WebAppConfig): ReferentielMRSCandidatPEConnect =
    new ReferentielMRSCandidatPEConnect(
      config = webAppConfig.referentielMRSCandidatPEConnectConfig,
      mrsValideesCSVLoader = mrsValideesCSVAdapter,
      mrsValideesPostgresSql = mrsValideesSqlAdapter,
      peConnectService = peConnectService
    )

  @Provides
  def referentielMRSCandidatLocal: ReferentielMRSCandidatLocal =
    new ReferentielMRSCandidatLocal

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
}
