package conf

import akka.actor.ActorSystem
import candidat.activite.infra.local.LocalEmailingDisponibilitesService
import candidat.activite.infra.mailjet.MailjetEmailingDisponibilitesService
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.{AppendOnlyStore, EventStore, EventStoreListener}
import fr.poleemploi.eventsourcing.infra.akka.AkkaEventStoreListener
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgreSQLAppendOnlyStore, PostgreSQLSnapshotStore, PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore
import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinService
import fr.poleemploi.perspectives.candidat.activite.infra.csv.{ActiviteCandidatCSVAdapter, ImportActiviteCandidatCsvAdapter}
import fr.poleemploi.perspectives.candidat.activite.infra.sql.DisponibiliteCandidatSqlAdapter
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.{HabiletesMRSCsvAdapter, ImportHabiletesMRSCsvAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocalAdapter, ImportMRSDHAELocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect._
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.csv.{ImportMRSValideeProspectCandidatCSVAdapter, MRSValideeProspectCandidatCSVAdapter}
import fr.poleemploi.perspectives.emailing.infra.local.LocalImportProspectService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetImportProspectService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.{MailjetWSAdapter, MailjetWSMapping}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.local.ReferentielMetierLocalAdapter
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.{CandidatProjectionElasticsearchQueryAdapter, CandidatProjectionElasticsearchQueryMapping}
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
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
  def activiteCandidatCSVAdapter(actorSystem: ActorSystem): ActiviteCandidatCSVAdapter =
    new ActiviteCandidatCSVAdapter(actorSystem = actorSystem)

  @Provides
  def importActiviteCandidatCsvAdapter(batchsConfig: BatchsConfig,
                                       actorSystem: ActorSystem,
                                       activiteCandidatCSVAdapter: ActiviteCandidatCSVAdapter): ImportActiviteCandidatCsvAdapter =
    new ImportActiviteCandidatCsvAdapter(
      config = batchsConfig.importPoleEmploiFileConfig,
      actorSystem = actorSystem,
      activiteCandidatCSVAdapter = activiteCandidatCSVAdapter
    )
  @Provides
  def candidatProjectionElasticsearchQueryMapping(referentielMetier: ReferentielMetier): CandidatProjectionElasticsearchQueryMapping =
    new CandidatProjectionElasticsearchQueryMapping(
      referentielMetier = referentielMetier
    )

  @Provides
  def candidatProjectionElasticsearchQueryAdapter(batchsConfig: BatchsConfig,
                                                  wsClient: WSClient,
                                                  mapping: CandidatProjectionElasticsearchQueryMapping): CandidatProjectionElasticsearchQueryAdapter =
    new CandidatProjectionElasticsearchQueryAdapter(
      wsClient = wsClient,
      esConfig = batchsConfig.esConfig,
      mapping = mapping
    )

  @Provides
  def peConnectSqlAdapter(database: Database): PEConnectSqlAdapter =
    new PEConnectSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def disponibiliteCandidatSqlAdapter(database: Database): DisponibiliteCandidatSqlAdapter =
    new DisponibiliteCandidatSqlAdapter(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  def recruteurProjectionSqlAdapter(database: Database): RecruteurProjectionSqlAdapter =
    new RecruteurProjectionSqlAdapter(
      database = database
    )

  // we do not use referentielMetier in batches so it's a dummy implementation
  @Provides
  @Singleton
  def referentielMetier: ReferentielMetier =
    new ReferentielMetierLocalAdapter

  @Provides
  def csvSqlAdapter(database: Database): CVSqlAdapter =
    new CVSqlAdapter(
      database = database,
      driver = PostgresDriver
    )

  @Provides
  @Singleton
  def autologinService(batchsConfig: BatchsConfig): AutologinService =
    new AutologinService(
      autologinConfig = batchsConfig.autologinConfig
    )

  @Provides
  @Singleton
  def mailjetEmailingDisponibilitesService(importActiviteCandidatCsvAdapter: ImportActiviteCandidatCsvAdapter,
                                           batchsConfig: BatchsConfig,
                                           actorSystem: ActorSystem,
                                           mailjetWSAdapter: MailjetWSAdapter,
                                           candidatQueryHandler: CandidatQueryHandler,
                                           autologinService: AutologinService,
                                           peConnectSqlAdapter: PEConnectSqlAdapter,
                                           disponibiliteCandidatSqlAdapter: DisponibiliteCandidatSqlAdapter): MailjetEmailingDisponibilitesService =
    new MailjetEmailingDisponibilitesService(
      baseUrl = batchsConfig.baseUrl,
      actorSystem = actorSystem,
      importFileAdapter = importActiviteCandidatCsvAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter,
      disponibiliteCandidatSqlAdapter = disponibiliteCandidatSqlAdapter,
      candidatQueryHandler = candidatQueryHandler,
      autologinService = autologinService,
      mailjetWSAdapter = mailjetWSAdapter
    )

  @Provides
  def localEmailingDisponibilitesService: LocalEmailingDisponibilitesService =
    new LocalEmailingDisponibilitesService

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
