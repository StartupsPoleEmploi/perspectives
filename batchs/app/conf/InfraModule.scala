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
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.{HabiletesMRSCsvAdapter, ImportHabiletesMRSCsvAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocalAdapter, ImportMRSDHAELocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect._
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.csv.{ImportMRSValideeProspectCandidatCSVAdapter, MRSValideeProspectCandidatCSVAdapter}
import fr.poleemploi.perspectives.emailing.infra.local.LocalImportProspectService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetImportProspectService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetWSAdapter, MailjetWSMapping}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InfraModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {}

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
  def mrsDHAEValideesCSVAdapter(actorSystem: ActorSystem): MRSDHAEValideesCSVAdapter =
    new MRSDHAEValideesCSVAdapter(actorSystem = actorSystem)

  @Provides
  def mrsDHAEValideesSqlAdapter(database: Database): MRSDHAEValideesSqlAdapter =
    new MRSDHAEValideesSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def mrsValideeProspectCandidatCSVAdapter(actorSystem: ActorSystem): MRSValideeProspectCandidatCSVAdapter =
    new MRSValideeProspectCandidatCSVAdapter(actorSystem = actorSystem)

  @Provides
  def importMRSValideeProspectCandidatCSVAdapter(batchsConfig: BatchsConfig,
                                                 actorSystem: ActorSystem,
                                                 mrsValideeProspectCandidatCSVAdapter: MRSValideeProspectCandidatCSVAdapter): ImportMRSValideeProspectCandidatCSVAdapter =
    new ImportMRSValideeProspectCandidatCSVAdapter(
      config = batchsConfig.importPoleEmploiFileConfig,
      actorSystem = actorSystem,
      mrsValideeProspectCandidatCSVAdapter = mrsValideeProspectCandidatCSVAdapter
    )

  @Provides
  def mailjetWSMapping: MailjetWSMapping =
    new MailjetWSMapping

  @Provides
  def mailjetWSAdapter(wsClient: WSClient,
                       batchsConfig: BatchsConfig,
                       mailjetWSMapping: MailjetWSMapping,
                       cacheApi: AsyncCacheApi): MailjetWSAdapter =
    new MailjetWSAdapter(
      wsClient = wsClient,
      config = batchsConfig.mailjetWSAdapterConfig,
      mapping = mailjetWSMapping,
      cacheApi = cacheApi
    )

  @Provides
  def importMRSDHAELocalAdapter: ImportMRSDHAELocalAdapter =
    new ImportMRSDHAELocalAdapter

  @Provides
  @Singleton
  def importMRSDHAEPEConnectAdapter(batchsConfig: BatchsConfig,
                                    actorSystem: ActorSystem,
                                    mrsDHAEValideesCSVAdapter: MRSDHAEValideesCSVAdapter,
                                    mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter): ImportMRSDHAEPEConnectAdapter =
    new ImportMRSDHAEPEConnectAdapter(
      config = batchsConfig.importPoleEmploiFileConfig,
      actorSystem = actorSystem,
      mrsDHAEValideesCSVAdapter = mrsDHAEValideesCSVAdapter,
      mrsDHAEValideesSqlAdapter = mrsDHAEValideesSqlAdapter
    )

  @Provides
  def mailjetSqlAdapter(database: Database): MailjetSqlAdapter =
    new MailjetSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def importProspectServiceMailjet(importMRSValideeProspectCandidatCSVAdapter: ImportMRSValideeProspectCandidatCSVAdapter,
                                   importMRSDHAEPEConnectAdapter: ImportMRSDHAEPEConnectAdapter,
                                   actorSystem: ActorSystem,
                                   mrsValideeProspectCandidatCSVAdapter: MRSValideeProspectCandidatCSVAdapter,
                                   mailjetSqlAdapter: MailjetSqlAdapter,
                                   mailjetWSAdapter: MailjetWSAdapter): MailjetImportProspectService =
    new MailjetImportProspectService(
      actorSystem = actorSystem,
      importMRSValideeProspectCandidatCSVAdapter = importMRSValideeProspectCandidatCSVAdapter,
      importMRSDHAEPEConnectAdapter = importMRSDHAEPEConnectAdapter,
      mailjetSQLAdapter = mailjetSqlAdapter,
      mailjetWSAdapter = mailjetWSAdapter
    )

  @Provides
  def localImportProspectService: LocalImportProspectService =
    new LocalImportProspectService

  @Provides
  def referentielHabiletesMRSSqlAdapter(database: Database): ReferentielHabiletesMRSSqlAdapter =
    new ReferentielHabiletesMRSSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def importHabiletesMRSLocalAdapter: ImportHabiletesMRSLocalAdapter =
    new ImportHabiletesMRSLocalAdapter

  @Provides
  def habiletesMRSCsvAdapter(actorSystem: ActorSystem): HabiletesMRSCsvAdapter =
    new HabiletesMRSCsvAdapter(actorSystem = actorSystem)

  @Provides
  def importHabiletesMRSCsvAdapter(batchsConfig: BatchsConfig,
                                   referentielHabiletesMRSSqlAdapter: ReferentielHabiletesMRSSqlAdapter,
                                   habiletesMRSCsvAdapter: HabiletesMRSCsvAdapter): ImportHabiletesMRSCsvAdapter =
    new ImportHabiletesMRSCsvAdapter(
      config = batchsConfig.importPoleEmploiFileConfig,
      habiletesMRSCsvAdapter = habiletesMRSCsvAdapter,
      referentielHabiletesMRSSqlAdapter = referentielHabiletesMRSSqlAdapter
    )
}
