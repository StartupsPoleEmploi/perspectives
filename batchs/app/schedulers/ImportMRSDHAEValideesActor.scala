package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSDHAE

import scala.concurrent.ExecutionContextExecutor

object ImportMRSDHAEValideesActor {

  final val name = "ImportMRSDHAEValideesActor"

  case object StartImportMRSValidees

  case object ImportMRSValideesDone

  def props(importMRS: ImportMRSDHAE): Props =
    Props(new ImportMRSDHAEValideesActor(
      importMRS = importMRS
    ))
}

class ImportMRSDHAEValideesActor(importMRS: ImportMRSDHAE) extends Actor with ActorLogging {

  import ImportMRSDHAEValideesActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportMRSValidees =>
      log.info("Intégration des MRS DHAE validées pour les candidats")
      importMRS.integrerMRSDHAEValidees.map(_ => ImportMRSValideesDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportMRSValidees =>
      log.warning("Import des MRS DHAE validées déjà en cours")
    case ImportMRSValideesDone =>
      log.info("Intégration des MRS DHAE valiées terminée")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des MRS DHAE validées")
      context.unbecome()
  }
}
