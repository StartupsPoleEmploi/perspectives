package fr.poleemploi.perspectives.commun.infra.play.http

import fr.poleemploi.cqrs.command.{Command, CommandHandler}
import fr.poleemploi.eventsourcing.Aggregate
import play.api.Logging
import play.api.mvc.{Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Command Handler permettant de gérer les exceptions lors des commandes et de renvoyer un résultat HTTP. <br />
  * Pas besoin de retourner un résultat vers l'interface : ce sont des situations exceptionnelles (l'UI ne valide pas un formulaire correctement avant, ou propose une fonctionnalité invalide pour un aggrégat)
  */
class HttpCommandHandler[A <: Aggregate](commandHandler: CommandHandler[A]) extends Results with Logging {

  def newId: A#Id = commandHandler.newId

  def handle(command: Command[A]): Future[Result] =
    commandHandler.handle(command)
      .map(_ => NoContent)
      .recoverWith {
        case t: Throwable =>
          logger.error(s"Erreur lors de l'exécution de la commande $command sur l'aggrégat ${command.id.value}", t)
          Future.successful(InternalServerError(s"Erreur lors de l'exécution de la commande $command sur l'aggrégat ${command.id.value} : ${t.getMessage}"))
      }
}