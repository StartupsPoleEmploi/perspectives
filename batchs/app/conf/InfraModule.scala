package conf

import akka.actor.ActorSystem
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore}
import fr.poleemploi.eventsourcing.infra.jackson.EventStoreObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgreSQLAppendOnlyStore, PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher, LocalEventHandler, LocalEventPublisher}
import fr.poleemploi.perspectives.authentification.infra.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.ImportMRSCandidatLocal
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.{ImportMRSCandidatPEConnect, MRSValideesCSVAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetWSAdapter, MailjetWSMapping}
import fr.poleemploi.perspectives.metier.infra.file.ReferentielMetierFileAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InfraModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
  }

  @Provides
  @Singleton
  def batchsConfig(configuration: Configuration): BatchsConfig =
    new BatchsConfig(configuration = configuration)

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
  def csvSqlAdapter(database: Database): CVSqlAdapter =
    new CVSqlAdapter(
      database = database,
      driver = PostgresDriver
    )

  @Provides
  def peConnectSqlAdapter(database: Database): PEConnectSqlAdapter =
    new PEConnectSqlAdapter(
      driver = PostgresDriver,
      database = database
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
                       batchsConfig: BatchsConfig,
                       mailjetWSMapping: MailjetWSMapping): MailjetWSAdapter =
    new MailjetWSAdapter(
      wsClient = wsClient,
      config = batchsConfig.mailjetWSAdapterConfig,
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
  def importMRSCandidatLocal: ImportMRSCandidatLocal =
    new ImportMRSCandidatLocal

  @Provides
  def importMRSCandidatPEConnectAdapter(batchsConfig: BatchsConfig,
                                        mrsValideesCSVAdapter: MRSValideesCSVAdapter,
                                        mrsValideesSqlAdapter: MRSValideesSqlAdapter,
                                        peConnectSqlAdapter: PEConnectSqlAdapter): ImportMRSCandidatPEConnect =
    new ImportMRSCandidatPEConnect(
      config = batchsConfig.importMRSCandidatPEConnectConfig,
      mrsValideesCSVAdapter = mrsValideesCSVAdapter,
      mrsValideesSqlAdapter = mrsValideesSqlAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter
    )

  @Provides
  def referentielMetierFileAdapter: ReferentielMetierFileAdapter =
    new ReferentielMetierFileAdapter()

  @Provides
  def referentielMetierWSAdapter(wsClient: WSClient,
                                 batchsConfig: BatchsConfig): ReferentielMetierWSAdapter =
    new ReferentielMetierWSAdapter(
      config = batchsConfig.referentielMetierWSAdapterConfig,
      wsClient = wsClient
    )
}
