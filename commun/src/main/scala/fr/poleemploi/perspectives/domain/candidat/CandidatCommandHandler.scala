package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.{AggregateId, Event}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatCommandHandler(candidatRepository: CandidatRepository)
  extends CommandHandler {

  def inscrire(command: InscrireCandidatCommand): Future[Unit] =
    execute(command.id, _.inscrire(command))

  def modifierCriteresRecherche(command: ModifierCriteresRechercheCommand): Future[Unit] =
    execute(command.id, _.modifierCriteres(command))

  private def execute(aggregateId: AggregateId, f: Candidat => List[Event]): Future[Unit] =
    candidatRepository.getById(aggregateId).map(candidat =>
      candidatRepository.save(candidat, f(candidat))
    )
}
