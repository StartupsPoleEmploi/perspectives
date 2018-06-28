package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.EventPublisher
import fr.poleemploi.perspectives.projections.infra.PostgresDriver
import fr.poleemploi.perspectives.projections.{CandidatProjection, CandidatQueryHandler}
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.JdbcBackend.Database

class RegisterProjections @Inject()(eventPublisher: EventPublisher,
                                    candidatProjection: CandidatProjection) {
  eventPublisher.register(candidatProjection)
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
}
