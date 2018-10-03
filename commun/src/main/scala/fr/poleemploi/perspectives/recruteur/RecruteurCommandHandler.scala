package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurCommandHandler(recruteurRepository: RecruteurRepository,
                              commentaireService: CommentaireService) extends CommandHandler {

  def newRecruteurId: RecruteurId = recruteurRepository.newRecruteurId

  def inscrire(command: InscrireRecruteurCommand): Future[Unit] =
    execute(command.id, c => Future(c.inscrire(command)))

  def modifierProfil(command: ModifierProfilCommand): Future[Unit] =
    execute(command.id, c => Future(c.modifierProfil(command)))

  def modifierProfilGerant(command: ModifierProfilGerantCommand): Future[Unit] =
    execute(command.id, c => Future(c.modifierProfilGerant(command)))

  def commenterListeCandidats(command: CommenterListeCandidatsCommand): Future[Unit] =
    execute(command.id, _.commenterListeCandidats(command, commentaireService))

  private def execute(recruteurId: RecruteurId, f: Recruteur => Future[List[Event]]): Future[Unit] =
    for {
      recruteur <- recruteurRepository.getById(recruteurId)
      events <- f(recruteur)
      _ <- recruteurRepository.save(recruteur, events)
    } yield ()
}
