package conf

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.eventsourcing.snapshotstore.SnapshotStore
import fr.poleemploi.eventsourcing.{AggregateRepository, Event}
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import javax.inject.Singleton

import scala.concurrent.Future

class EventSourcingModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def candidatSnapshotRepository(snapshotStore: SnapshotStore,
                                 @Named("eventSourcingObjectMapper")
                                 objectMapper: ObjectMapper): CandidatSnapshotRepository =
    new CandidatSnapshotRepository(
      snapshotStore = snapshotStore,
      objectMapper = objectMapper
    )

  @Provides
  @Singleton
  def candidatRepository(eventStore: EventStore,
                         snapshotRepository: CandidatSnapshotRepository): CandidatRepository =
    new CandidatRepository(
      eventStore = eventStore,
      snapshotRepository = snapshotRepository
    )

  @Provides
  @Singleton
  def candidatCommandHandler(candidatRepository: CandidatRepository,
                             referentielHabiletesMRS: ReferentielHabiletesMRS): CandidatCommandHandler =
    new CandidatCommandHandler {
      override val repository: AggregateRepository[Candidat] = candidatRepository

      override def configure: PartialFunction[Command[Candidat], Candidat => Future[List[Event]]] = {
        case command: AjouterMRSValideesCommand => c => c.ajouterMRSValidee(command, referentielHabiletesMRS)
      }
    }
}
