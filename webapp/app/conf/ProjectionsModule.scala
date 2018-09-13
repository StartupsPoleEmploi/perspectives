package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat.infra.sql.CandidatProjectionSqlAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatEmailProjection, CandidatNotificationSlackProjection, CandidatProjection, CandidatQueryHandler}
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
import fr.poleemploi.perspectives.projections.recruteur.{RecruteurEmailProjection, RecruteurProjection, RecruteurQueryHandler}
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
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
  eventHandler.subscribe(candidatMailProjection)
  eventHandler.subscribe(recruteurProjection)
  eventHandler.subscribe(recruteurEmailProjection)

  if (webAppConfig.useSlackNotificationCandidat) {
    eventHandler.subscribe(candidatNotificationSlackProjection)
  }
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjectionSqlAdapter(database: Database,
                                   referentielMetier: ReferentielMetier,
                                   rechercheCandidatService: RechercheCandidatService,
                                   webAppConfig: WebAppConfig): CandidatProjectionSqlAdapter =
    new CandidatProjectionSqlAdapter(
      database = database,
      referentielMetier = referentielMetier,
      rechercheCandidatService = rechercheCandidatService,
      candidatsTesteurs = webAppConfig.candidatsTesteurs.map(CandidatId)
    )

  @Provides
  @Singleton
  def candidatProjection(candidatProjectionSqlAdapter: CandidatProjectionSqlAdapter): CandidatProjection =
    new CandidatProjection(
      adapter = candidatProjectionSqlAdapter
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
  def recruteurProjectionSqlAdapter(database: Database): RecruteurProjectionSqlAdapter =
    new RecruteurProjectionSqlAdapter(
      database = database
    )

  @Provides
  @Singleton
  def recruteurProjection(recruteurProjectionSqlAdapter: RecruteurProjectionSqlAdapter): RecruteurProjection =
    new RecruteurProjection(
      adapter = recruteurProjectionSqlAdapter
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
  def rechercheCandidatQueryHandler(rechercheCandidatService: RechercheCandidatService): RechercheCandidatQueryHandler =
    new RechercheCandidatQueryHandler(
      rechercheCandidatService = rechercheCandidatService
    )

}
