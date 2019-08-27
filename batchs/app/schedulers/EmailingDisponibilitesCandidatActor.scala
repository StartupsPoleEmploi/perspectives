package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import candidat.activite.domain.EmailingDisponibilitesService

import scala.concurrent.ExecutionContextExecutor

object EmailingDisponibilitesCandidatActor {

  final val name = "EmailingDisponibilitesCandidatActor"

  case object StartEmailingDisponibilitesCandidat

  case object EmailingDisponibilitesCandidatDone

  def props(emailingDisponibilitesService: EmailingDisponibilitesService): Props =
    Props(new EmailingDisponibilitesCandidatActor(
      emailingDisponibilitesService = emailingDisponibilitesService
    ))
}

class EmailingDisponibilitesCandidatActor(emailingDisponibilitesService: EmailingDisponibilitesService) extends Actor with ActorLogging {

  import EmailingDisponibilitesCandidatActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartEmailingDisponibilitesCandidat =>
      log.info("Emailing des disponibilités candidat")
      emailingDisponibilitesService.envoyerEmailsDisponibilites.map(_ => EmailingDisponibilitesCandidatDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartEmailingDisponibilitesCandidat =>
      log.warning("Emailing des disponibilités candidat déjà en cours")
    case EmailingDisponibilitesCandidatDone =>
      log.info("Emailing des disponibilités candidat terminé")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'emailing des disponibilités candidat")
      context.unbecome()
  }
}
