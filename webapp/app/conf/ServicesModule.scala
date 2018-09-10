package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat
import fr.poleemploi.perspectives.candidat.mrs.infra.local.ReferentielMRSCandidatLocal
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSCandidatPEConnect
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.conseiller.{AutorisationService, ConseillerId}
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetEmailAdapter
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.file.ReferentielMetierFileAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
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
  def referentielMRSCandidat(referentielMRSCandidatPEConnect: Provider[ReferentielMRSCandidatPEConnect],
                             referentielMRSCandidatLocal: Provider[ReferentielMRSCandidatLocal],
                             webAppConfig: WebAppConfig): ReferentielMRSCandidat =
    if (webAppConfig.usePEConnect)
      referentielMRSCandidatPEConnect.get()
    else
      referentielMRSCandidatLocal.get()

  @Provides
  @Singleton
  def emailingService(mailjetContactAdapter: MailjetSqlAdapter,
                      mailjetEmailAdapter: MailjetEmailAdapter): EmailingService =
    new MailjetEmailingService(
      mailjetContactAdapter = mailjetContactAdapter,
      mailjetEmailAdapter = mailjetEmailAdapter
    )

  @Provides
  @Singleton
  def referentielMetier(referentielMetierWSAdapter: Provider[ReferentielMetierWSAdapter],
                        referentielMetierFileAdapter: Provider[ReferentielMetierFileAdapter],
                        webAppConfig: WebAppConfig): ReferentielMetier =
    if (webAppConfig.useReferentielMetierWS)
      referentielMetierWSAdapter.get()
    else
      referentielMetierFileAdapter.get()
}
