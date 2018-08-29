package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurCommandHandler(recruteurRepository: RecruteurRepository)
  extends CommandHandler {

  def newRecruteurId: RecruteurId = recruteurRepository.newRecruteurId

  def inscrire(command: InscrireRecruteurCommand): Future[Unit] =
    execute(command.id, _.inscrire(command))

  def modifierProfil(command: ModifierProfilCommand): Future[Unit] =
    execute(command.id, _.modifierProfil(command))

  def modifierProfilGerant(command: ModifierProfilGerantCommand): Future[Unit] =
    execute(command.id, _.modifierProfilGerant(command))

  private def execute(recruteurId: RecruteurId, f: Recruteur => List[Event]): Future[Unit] =
    recruteurRepository.getById(recruteurId).map(recruteur =>
      recruteurRepository.save(recruteur, f(recruteur))
    )
}
