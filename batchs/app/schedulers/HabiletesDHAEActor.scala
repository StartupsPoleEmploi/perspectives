package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.dhae.domain.ImportHabiletesDHAE

import scala.concurrent.ExecutionContextExecutor

object HabiletesDHAEActor {

  final val name = "HabiletesDHAEActor"

  case object StartImportHabiletesDHAE

  case object ImportHabiletesDHAEDone

  def props(importHabiletesDHAE: ImportHabiletesDHAE): Props =
    Props(new HabiletesDHAEActor(
      importHabiletesDHAE = importHabiletesDHAE
    ))
}

class HabiletesDHAEActor(importHabiletesDHAE: ImportHabiletesDHAE) extends Actor with ActorLogging {

  import HabiletesDHAEActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportHabiletesDHAE =>
      log.info("Intégration des Habiletés DHAE")
      importHabiletesDHAE.integrerHabiletesDHAE.map(_ => ImportHabiletesDHAEDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportHabiletesDHAE =>
      log.warning("Import des Habiletés DHAE déjà en cours")
    case ImportHabiletesDHAEDone =>
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des Habiletés DHAE")
      context.unbecome()
  }
}