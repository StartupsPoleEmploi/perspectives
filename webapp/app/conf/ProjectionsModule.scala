package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.EventPublisher
import fr.poleemploi.perspectives.projections.infra.PostgresDriver
import fr.poleemploi.perspectives.projections.{CandidatNotificationSlackProjection, CandidatProjection, CandidatQueryHandler}
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import slick.jdbc.JdbcBackend.Database

class RegisterProjections @Inject()(eventPublisher: EventPublisher,
                                    candidatProjection: CandidatProjection,
                                    candidatNotificationSlackProjection: CandidatNotificationSlackProjection,
                                    webAppConfig: WebAppConfig) {
  eventPublisher.register(candidatProjection)

  if (webAppConfig.useSlackNotificationCandidat) {
    eventPublisher.register(candidatNotificationSlackProjection)
  }
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjection(database: Database): CandidatProjection =
    new CandidatProjection(
      driver = PostgresDriver,
      database = database
    )

  @Provides
  @Singleton
  def candidatQueryHandler(candidatProjection: CandidatProjection): CandidatQueryHandler =
    new CandidatQueryHandler(
      candidatProjection = candidatProjection
    )

  @Provides
  @Singleton
  def CandidatNotificationSlackProjection(webAppConfig: WebAppConfig,
                                          wsClient: WSClient): CandidatNotificationSlackProjection =
    new CandidatNotificationSlackProjection(
      slackCandidatConfig = webAppConfig.slackCandidatConfig,
      wsClient = wsClient
    )
}
