package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur.alerte.domain.AlerteId
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RecruteurCommandHandler(recruteurRepository: RecruteurRepository,
                              commentaireService: CommentaireService) extends CommandHandler {

  def newRecruteurId: RecruteurId = recruteurRepository.newRecruteurId

  def newAlerteId: AlerteId = recruteurRepository.newAlerteId

  def inscrire(command: InscrireRecruteurCommand): Future[Unit] =
    execute(command.id, r => Future(r.inscrire(command)))

  def modifierProfil(command: ModifierProfilCommand): Future[Unit] =
    execute(command.id, r => Future(r.modifierProfil(command)))

  def connecter(command: ConnecterRecruteurCommand): Future[Unit] =
    execute(command.id, r => Future(r.connecter(command)))

  def commenterListeCandidats(command: CommenterListeCandidatsCommand): Future[Unit] =
    execute(command.id, _.commenterListeCandidats(command, commentaireService))

  def creerAlerte(command: CreerAlerteCommand): Future[Unit] =
    execute(command.id, r => Future(r.creerAlerte(command)))

  def supprimerAlerte(command: SupprimerAlerteCommand): Future[Unit] =
    execute(command.id, r => Future(r.supprimerAlerte(command)))

  private def execute(recruteurId: RecruteurId, f: Recruteur => Future[List[Event]]): Future[Unit] =
    for {
      recruteur <- recruteurRepository.getById(recruteurId)
      events <- f(recruteur)
      _ <- recruteurRepository.save(recruteur, events)
    } yield ()
}
