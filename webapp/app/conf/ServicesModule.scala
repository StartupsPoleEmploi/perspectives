package conf

import com.google.inject.{AbstractModule, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat
import fr.poleemploi.perspectives.candidat.mrs.infra.ReferentielMRSCandidatLocal
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.conseiller.{AutorisationService, ConseillerId}
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetEmailAdapter
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWS
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcBackend.Database

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def autorisationService(webAppConfig: WebAppConfig): AutorisationService =
    new AutorisationService(
      admins = webAppConfig.admins.map(ConseillerId)
    )

  @Provides
  @Singleton
  def cvService(database: Database): CVService =
    new CVSqlAdapter(
      database = database,
      driver = PostgresDriver
    )

  @Provides
  @Singleton
  def referentielMetierEvalue(metierEvalueCSVLoader: MRSValideesCSVAdapter,
                              postgresqlMetierEvalueService: MRSValideesSqlAdapter,
                              webAppConfig: WebAppConfig): ReferentielMRSCandidat =
    new ReferentielMRSCandidatLocal(
      referentielMRSCandidatConfig = webAppConfig.referentielMRSCandidatConfig,
      mrsValideesCSVLoader = metierEvalueCSVLoader,
      mrsValideesPostgresSql = postgresqlMetierEvalueService
    )

  @Provides
  @Singleton
  def referentielMetier(wsClient: WSClient,
                        webAppConfig: WebAppConfig): ReferentielMetier =
    new ReferentielMetierWS(
      wsClient = wsClient,
      referentielMetierWSConfig = webAppConfig.referentielMetierWSAdapterConfig
    )

  @Provides
  @Singleton
  def emailingService(mailjetContactAdapter: MailjetSqlAdapter,
                      mailjetEmailAdapter: MailjetEmailAdapter): EmailingService =
    new MailjetEmailingService(
      mailjetContactAdapter = mailjetContactAdapter,
      mailjetEmailAdapter = mailjetEmailAdapter
    )
}
