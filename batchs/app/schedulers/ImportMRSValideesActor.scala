package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRS

import scala.concurrent.ExecutionContextExecutor

object ImportMRSValideesActor {

  final val name = "ImportMRSValideesActor"

  case object StartImportMRSValidees

  case object ImportMRSValideesDone

  def props(importMRS: ImportMRS): Props =
    Props(new ImportMRSValideesActor(
      importMRS = importMRS
    ))
}

class ImportMRSValideesActor(importMRS: ImportMRS) extends Actor with ActorLogging {

  import ImportMRSValideesActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportMRSValidees =>
      log.info("Intégration des MRS validées pour les candidats")
      (for {
        _ <- importMRS.integrerMRSValidees
        _ <- importMRS.integrerMRSDHAEValidees
      } yield ImportMRSValideesDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportMRSValidees =>
      log.warning("Import des MRS validées déjà en cours")
    case ImportMRSValideesDone =>
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des MRS validées")
      context.unbecome()
  }
}
