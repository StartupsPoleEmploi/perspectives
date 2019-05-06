package fr.poleemploi.perspectives.recruteur.state

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RecruteurState {

  def inscrire(context: RecruteurContext, command: InscrireRecruteurCommand): List[Event] =
    default(context, command)

  def modifierProfil(context: RecruteurContext, command: ModifierProfilCommand): List[Event] =
    default(context, command)

  def connecter(context: RecruteurContext, command: ConnecterRecruteurCommand): List[Event] =
    default(context, command)

  def commenterListeCandidats(context: RecruteurContext,
                              command: CommenterListeCandidatsCommand,
                              commentaireService: CommentaireService): Future[List[Event]] =
    Future(default(context, command))

  private def default(context: RecruteurContext, command: Command[Recruteur]) =
    throw new IllegalStateException(s"Le recruteur ${command.id.value} avec le statut ${context.statut.value} ne peut pas gérer la commande ${command.getClass.getSimpleName}")
}
