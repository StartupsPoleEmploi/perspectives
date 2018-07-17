package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.{AggregateId, Event}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurCommandHandler(recruteurRepository: RecruteurRepository)
  extends CommandHandler {

  def inscrire(command: InscrireRecruteurCommand): Future[Unit] =
    execute(command.id, _.inscrire(command))

  def modifierProfil(command: ModifierProfilCommand): Future[Unit] =
    execute(command.id, _.modifierProfil(command))

  private def execute(aggregateId: AggregateId, f: Recruteur => List[Event]): Future[Unit] =
    recruteurRepository.getById(aggregateId).map(candidat =>
      recruteurRepository.save(candidat, f(candidat))
    )
}
