package conf

import com.google.inject._
import fr.poleemploi.eventsourcing.eventstore.EventStoreListener
import fr.poleemploi.perspectives.projections.candidat.CandidatProjection
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.CandidatProjectionElasticsearchAdapter
import net.codingwell.scalaguice.ScalaModule

class RegisterProjections @Inject()(eventStoreListener: EventStoreListener,
                                    candidatProjection: CandidatProjection) {
  eventStoreListener.subscribe(candidatProjection)
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjection(candidatProjectionElasticsearchAdapter: CandidatProjectionElasticsearchAdapter): CandidatProjection =
    candidatProjectionElasticsearchAdapter

}

