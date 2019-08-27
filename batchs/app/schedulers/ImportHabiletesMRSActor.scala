package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportHabiletesMRS

import scala.concurrent.ExecutionContextExecutor

object ImportHabiletesMRSActor {

  final val name = "ImportHabiletesMRSActor"

  case object StartImportHabiletesMRS

  case object ImportHabiletesMRSDone

  def props(importHabiletesMRS: ImportHabiletesMRS): Props =
    Props(new ImportHabiletesMRSActor(
      importHabiletesMRS = importHabiletesMRS
    ))
}

class ImportHabiletesMRSActor(importHabiletesMRS: ImportHabiletesMRS) extends Actor with ActorLogging {

  import ImportHabiletesMRSActor._

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
      log.info("Intégration des Habiletés MRS terminée")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des Habiletés MRS")
      context.unbecome()
  }
}
