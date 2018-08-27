package conf

import com.google.inject.{AbstractModule, Provides, Singleton}
import fr.poleemploi.perspectives.domain.candidat.cv.CVService
import fr.poleemploi.perspectives.domain.candidat.cv.infra.PostgresqlCVService
import fr.poleemploi.perspectives.domain.candidat.mrs.ReferentielMRSCandidat
import fr.poleemploi.perspectives.domain.candidat.mrs.infra.{MRSValideesCSVLoader, MRSValideesPostgreSql, ReferentielMRSCandidatLocal}
import fr.poleemploi.perspectives.domain.conseiller.{AutorisationService, ConseillerId}
import fr.poleemploi.perspectives.domain.emailing.EmailingService
import fr.poleemploi.perspectives.domain.emailing.infra.mailjet.{MailjetContactAdapter, MailjetEmailAdapter, MailjetEmailingService}
import fr.poleemploi.perspectives.domain.metier.ReferentielMetier
import fr.poleemploi.perspectives.domain.metier.infra.ReferentielMetierWS
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
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
    new PostgresqlCVService(
      database = database,
      driver = PostgresDriver
    )

  @Provides
  @Singleton
  def referentielMetierEvalue(metierEvalueCSVLoader: MRSValideesCSVLoader,
                              postgresqlMetierEvalueService: MRSValideesPostgreSql,
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
      referentielMetierWSConfig = webAppConfig.referentielMetierWSConfig
    )

  @Provides
  @Singleton
  def emailingService(mailjetContactAdapter: MailjetContactAdapter,
                      mailjetEmailAdapter: MailjetEmailAdapter): EmailingService =
    new MailjetEmailingService(
      mailjetContactAdapter = mailjetContactAdapter,
      mailjetEmailAdapter = mailjetEmailAdapter
    )
}
