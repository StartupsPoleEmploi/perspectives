package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.perspectives.domain.demandeurEmploi.{DemandeurEmploiCommandHandler, DemandeurEmploiRepository}
import javax.inject.Singleton

class EventSourcingModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def demandeurEmploiRepository(eventStore: EventStore): DemandeurEmploiRepository =
    new DemandeurEmploiRepository(
      eventStore = eventStore
    )

  @Provides
  @Singleton
  def demandeurEmploiCommandHandler(demandeurEmploiRepository: DemandeurEmploiRepository): DemandeurEmploiCommandHandler =
    new DemandeurEmploiCommandHandler(
      demandeurEmploiRepository = demandeurEmploiRepository
    )
}
