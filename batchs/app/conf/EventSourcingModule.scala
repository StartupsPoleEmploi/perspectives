package conf

import com.google.inject.{AbstractModule, Provides}
import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.eventstore.EventStore
import fr.poleemploi.eventsourcing.{AggregateRepository, Event}
import fr.poleemploi.perspectives.candidat._
import javax.inject.Singleton

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EventSourcingModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def candidatRepository(eventStore: EventStore): CandidatRepository =
    new CandidatRepository(
      eventStore = eventStore
    )

  @Provides
  @Singleton
  def candidatCommandHandler(candidatRepository: CandidatRepository): CandidatCommandHandler =
    new CandidatCommandHandler {
      override def repository: AggregateRepository[Candidat] = candidatRepository

      override def newCandidatId: CandidatId = candidatRepository.newCandidatId

      override def configure: PartialFunction[Command[Candidat], Candidat => Future[List[Event]]] = {
        case command: AjouterMRSValideesCommand => c => Future(c.ajouterMRSValidee(command))
      }
    }
}
