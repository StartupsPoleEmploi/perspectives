package conf

import akka.actor.ActorSystem
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore, EventStoreListener}
import fr.poleemploi.eventsourcing.infra.akka.AkkaEventStoreListener
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgreSQLAppendOnlyStore, PostgreSQLSnapshotStore, PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore
import fr.poleemploi.perspectives.candidat.dhae.infra.csv.{HabiletesDHAECsvAdapter, ImportHabiletesDHAECsvAdapter}
import fr.poleemploi.perspectives.candidat.dhae.infra.local.ImportHabiletesDHAELocal
import fr.poleemploi.perspectives.candidat.dhae.infra.sql.ReferentielHabiletesDHAESqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.{HabiletesMRSCsvAdapter, ImportHabiletesMRSCsvAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocal, ImportMRSCandidatLocal}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.{ImportMRSCandidatPEConnect, MRSValideesCandidatsCSVAdapter, MRSValideesCandidatsSqlAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.play.cache.InMemoryCacheApi
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetWSAdapter, MailjetWSMapping}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.elasticsearch.ReferentielMetierElasticsearchAdapter
import fr.poleemploi.perspectives.metier.infra.ws.{ReferentielMetierWSAdapter, ReferentielMetierWSMapping}
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.{CandidatProjectionElasticsearchAdapter, CandidatProjectionElasticsearchMapping}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.cache.AsyncCacheApi
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
  def peConnectSqlAdapter(database: Database): PEConnectSqlAdapter =
    new PEConnectSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def mrsValideesCandidatsCSVAdapter(actorSystem: ActorSystem): MRSValideesCandidatsCSVAdapter =
    new MRSValideesCandidatsCSVAdapter(actorSystem = actorSystem)

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
                                        actorSystem: ActorSystem,
                                        mrsValideesCandidatsCSVAdapter: MRSValideesCandidatsCSVAdapter,
                                        mrsValideesCandidatsSqlAdapter: MRSValideesCandidatsSqlAdapter,
                                        peConnectSqlAdapter: PEConnectSqlAdapter): ImportMRSCandidatPEConnect =
    new ImportMRSCandidatPEConnect(
      config = batchsConfig.importMRSCandidatPEConnectConfig,
      actorSystem = actorSystem,
      mrsValideesCandidatsCSVAdapter = mrsValideesCandidatsCSVAdapter,
      mrsValideesCandidatsSqlAdapter = mrsValideesCandidatsSqlAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter
    )

  @Provides
  def referentielMetierElasticsearchAdapter(wsClient: WSClient,
                                            batchsConfig: BatchsConfig): ReferentielMetierElasticsearchAdapter =
    new ReferentielMetierElasticsearchAdapter(
      esConfig = batchsConfig.esConfig,
      wsClient = wsClient
    )

  @Provides
  def referentielMetierWSMapping: ReferentielMetierWSMapping =
    new ReferentielMetierWSMapping()

  @Provides
  def referentielMetierWSAdapter(wsClient: WSClient,
                                 mapping: ReferentielMetierWSMapping,
                                 batchsConfig: BatchsConfig,
                                 cacheApi: AsyncCacheApi,
                                 referentielMetierElasticsearchAdapter: ReferentielMetierElasticsearchAdapter): ReferentielMetierWSAdapter =
    new ReferentielMetierWSAdapter(
      config = batchsConfig.referentielMetierWSAdapterConfig,
      mapping = mapping,
      wsClient = wsClient,
      cacheApi = cacheApi,
      elasticsearchAdapter = referentielMetierElasticsearchAdapter
    )

  @Provides
  def referentielHabiletesMRSSqlAdapter(database: Database): ReferentielHabiletesMRSSqlAdapter =
    new ReferentielHabiletesMRSSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def importHabiletesMRSLocal: ImportHabiletesMRSLocal =
    new ImportHabiletesMRSLocal

  @Provides
  def habiletesMRSCsvAdapter(actorSystem: ActorSystem): HabiletesMRSCsvAdapter =
    new HabiletesMRSCsvAdapter(actorSystem = actorSystem)

  @Provides
  def importHabiletesMRSCsvAdapter(batchsConfig: BatchsConfig,
                                   referentielHabiletesMRSSqlAdapter: ReferentielHabiletesMRSSqlAdapter,
                                   habiletesMRSCsvAdapter: HabiletesMRSCsvAdapter): ImportHabiletesMRSCsvAdapter =
    new ImportHabiletesMRSCsvAdapter(
      config = batchsConfig.importHabiletesMRSCsvAdapterConfig,
      habiletesMRSCsvAdapter = habiletesMRSCsvAdapter,
      referentielHabiletesMRSSqlAdapter = referentielHabiletesMRSSqlAdapter
    )

  @Provides
  def importHabiletesDHAELocal: ImportHabiletesDHAELocal =
    new ImportHabiletesDHAELocal

  @Provides
  def habiletesDHAECsvAdapter(actorSystem: ActorSystem): HabiletesDHAECsvAdapter =
    new HabiletesDHAECsvAdapter(actorSystem = actorSystem)

  @Provides
  def referentielHabiletesDHAESqlAdapter(database: Database): ReferentielHabiletesDHAESqlAdapter =
    new ReferentielHabiletesDHAESqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def importHabiletesDHAELocal(batchsConfig: BatchsConfig,
                               habiletesDHAECsvAdapter: HabiletesDHAECsvAdapter,
                               referentielHabiletesDHAESqlAdapter: ReferentielHabiletesDHAESqlAdapter): ImportHabiletesDHAECsvAdapter =
    new ImportHabiletesDHAECsvAdapter(
      habiletesDHAECsvAdapter = habiletesDHAECsvAdapter,
      referentielHabiletesDHAESqlAdapter = referentielHabiletesDHAESqlAdapter,
      config = batchsConfig.importHabiletesDHAECsvAdapterConfig
    )

  @Provides
  def candidatProjectionElasticsearchMapping(referentielMetier: ReferentielMetier): CandidatProjectionElasticsearchMapping =
    new CandidatProjectionElasticsearchMapping(
      referentielMetier = referentielMetier
    )

  @Provides
  def candidatProjectionElasticsearchAdapter(batchsConfig: BatchsConfig,
                                             wsClient: WSClient,
                                             mapping: CandidatProjectionElasticsearchMapping): CandidatProjectionElasticsearchAdapter =
    new CandidatProjectionElasticsearchAdapter(
      wsClient = wsClient,
      esConfig = batchsConfig.esConfig,
      mapping = mapping
    )
}
