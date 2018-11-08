package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportHabiletesMRS

import scala.concurrent.ExecutionContextExecutor

object HabiletesMRSActor {

  final val name = "HabiletesMRSActor"

  case object StartImportHabiletesMRS

  case object ImportHabiletesMRSDone

  def props(importHabiletesMRS: ImportHabiletesMRS): Props =
    Props(new HabiletesMRSActor(
      importHabiletesMRS = importHabiletesMRS
    ))
}

class HabiletesMRSActor(importHabiletesMRS: ImportHabiletesMRS) extends Actor with ActorLogging {

  import HabiletesMRSActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportHabiletesMRS =>
      log.info("Intégration des Habiletés MRS")
      importHabiletesMRS.integrerHabiletesMRS.map(_ => ImportHabiletesMRSDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportHabiletesMRS =>
      log.warning("Import des Habiletés MRS déjà en cours")
    case ImportHabiletesMRSDone =>
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des Habiletés MRS")
      context.unbecome()
  }
}
