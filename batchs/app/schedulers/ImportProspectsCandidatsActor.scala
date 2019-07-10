package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.emailing.domain.ImportProspectService

import scala.concurrent.ExecutionContextExecutor

object ImportProspectsCandidatsActor {

  final val name = "ImportProspectsCandidatsActor"

  case object StartImportProspectsCandidats

  case object ImportProspectsCandidatsDone

  def props(importProspectService: ImportProspectService): Props =
    Props(new ImportProspectsCandidatsActor(
      importProspectService = importProspectService
    ))
}

class ImportProspectsCandidatsActor(importProspectService: ImportProspectService) extends Actor with ActorLogging {

  import ImportProspectsCandidatsActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportProspectsCandidats =>
      log.info("Intégration des prospects candidats")
      importProspectService.importerProspectsCandidats.map(_ => ImportProspectsCandidatsDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportProspectsCandidats =>
      log.warning("Import des prospects candidats déjà en cours")
    case ImportProspectsCandidatsDone =>
      log.info("Intégration des prospects candidats terminéé")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des prospects candidats")
      context.unbecome()
  }

}
