package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import candidat.activite.domain.EmailingCandidatsJVRService

import scala.concurrent.ExecutionContextExecutor

object EmailingCandidatsJVRActor {

  final val name = "EmailingCandidatsJVRActor"

  case object StartEmailingCandidatsJVR

  case object EmailingCandidatsJVRDone

  def props(emailingCandidatsJVRService: EmailingCandidatsJVRService): Props =
    Props(new EmailingCandidatsJVRActor(
      emailingCandidatsJVRService = emailingCandidatsJVRService
    ))
}

class EmailingCandidatsJVRActor(emailingCandidatsJVRService: EmailingCandidatsJVRService) extends Actor with ActorLogging {

  import EmailingCandidatsJVRActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartEmailingCandidatsJVR =>
      log.info("Emailing des candidats JVR")
      emailingCandidatsJVRService.envoyerEmailsCandidatsJVR.map(_ => EmailingCandidatsJVRDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartEmailingCandidatsJVR =>
      log.warning("Emailing des candidats JVR déjà en cours")
    case EmailingCandidatsJVRDone =>
      log.info("Emailing des candidats JVR terminé")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'emailing des candidats JVR")
      context.unbecome()
  }
}
