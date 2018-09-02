package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat.{CandidatEmailProjection, CandidatNotificationSlackProjection, CandidatProjection, CandidatQueryHandler}
import fr.poleemploi.perspectives.projections.metier.MetierQueryHandler
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurEmailProjection, RecruteurProjection, RecruteurQueryHandler}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcBackend.Database

class RegisterProjections @Inject()(eventPublisher: EventPublisher,
                                    eventHandler: EventHandler,
                                    candidatProjection: CandidatProjection,
                                    candidatNotificationSlackProjection: CandidatNotificationSlackProjection,
                                    candidatMailProjection: CandidatEmailProjection,
                                    recruteurProjection: RecruteurProjection,
                                    recruteurEmailProjection: RecruteurEmailProjection,
                                    webAppConfig: WebAppConfig) {
  eventPublisher.subscribe(eventHandler)

  eventHandler.subscribe(candidatProjection)
  eventHandler.subscribe(recruteurProjection)

  if (webAppConfig.useSlackNotificationCandidat) {
    eventHandler.subscribe(candidatNotificationSlackProjection)
  }
  if (webAppConfig.useEmail) {
    eventHandler.subscribe(candidatMailProjection)
    eventHandler.subscribe(recruteurEmailProjection)
  }
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjection(database: Database,
                         referentielMetier: ReferentielMetier,
                         webAppConfig: WebAppConfig): CandidatProjection =
    new CandidatProjection(
      driver = PostgresDriver,
      database = database,
      candidatsTesteurs = webAppConfig.candidatsTesteurs.map(CandidatId),
      referentielMetier = referentielMetier
    )

  @Provides
  @Singleton
  def candidatQueryHandler(candidatProjection: CandidatProjection,
                           recruteurProjection: RecruteurProjection,
                           cvService: CVService): CandidatQueryHandler =
    new CandidatQueryHandler(
      candidatProjection = candidatProjection,
      recruteurProjection = recruteurProjection,
      cvService = cvService
    )

  @Provides
  @Singleton
  def candidatNotificationSlackProjection(webAppConfig: WebAppConfig,
                                          wsClient: WSClient): CandidatNotificationSlackProjection =
    new CandidatNotificationSlackProjection(
      slackCandidatConfig = webAppConfig.slackCandidatConfig,
      wsClient = wsClient
    )

  @Provides
  @Singleton
  def candidatEmailProjection(emailingService: EmailingService): CandidatEmailProjection =
    new CandidatEmailProjection(
      emailingService = emailingService
    )

  @Provides
  @Singleton
  def recruteurProjection(database: Database): RecruteurProjection =
    new RecruteurProjection(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def recruteurEmailProjection(emailingService: EmailingService): RecruteurEmailProjection =
    new RecruteurEmailProjection(
      emailingService = emailingService
    )

  @Provides
  @Singleton
  def recruteurQueryHandler(recruteurProjection: RecruteurProjection): RecruteurQueryHandler =
    new RecruteurQueryHandler(
      recruteurProjection = recruteurProjection
    )

  @Provides
  @Singleton
  def metierQueryHandler(referentielMetier: ReferentielMetier): MetierQueryHandler =
    new MetierQueryHandler(
      referentielMetier = referentielMetier
    )

}
