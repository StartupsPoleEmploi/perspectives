package fr.poleemploi.perspectives.commun.infra.play.http

import fr.poleemploi.cqrs.command.{Command, CommandHandler}
import fr.poleemploi.eventsourcing.Aggregate
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HttpCommandHandler[A <: Aggregate](commandHandler: CommandHandler[A]) extends Results {

  def handle(command: Command[A]): Future[Result] =
    commandHandler.handle(command)
      .map(_ => NoContent)
      .recoverWith {
        case e: IllegalArgumentException => Future.successful(BadRequest(Json.obj("globalError" -> e.getMessage)))
      }

}
