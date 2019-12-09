package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import candidat.activite.domain.ImportOffresEnDifficulteGereesParConseillerService
import schedulers.ImportOffresEnDifficulteGereesParConseillerActor.{ImportOffresEnDifficulteGereesParConseillerDone, StartImportOffresEnDifficulteGereesParConseiller}

import scala.concurrent.ExecutionContextExecutor

object ImportOffresEnDifficulteGereesParConseillerActor {

  final val name = "ImportOffresEnDifficulteGereesParConseillerActor"

  case object StartImportOffresEnDifficulteGereesParConseiller

  case object ImportOffresEnDifficulteGereesParConseillerDone

  def props(ImportOffresEnDifficulteGereesParConseillerService: ImportOffresEnDifficulteGereesParConseillerService): Props =
    Props(new ImportOffresEnDifficulteGereesParConseillerActor(
      importOffresEnDifficulteGereesParConseillerService = ImportOffresEnDifficulteGereesParConseillerService
    ))
}

class ImportOffresEnDifficulteGereesParConseillerActor(importOffresEnDifficulteGereesParConseillerService: ImportOffresEnDifficulteGereesParConseillerService) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportOffresEnDifficulteGereesParConseiller =>
      log.info("Intégration des offres en difficulté gérées par les conseillers")
      importOffresEnDifficulteGereesParConseillerService.importerOffresEnDifficulteGereesParConseiller.map(_ => ImportOffresEnDifficulteGereesParConseillerDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportOffresEnDifficulteGereesParConseiller =>
      log.warning("Import des offres en difficulté gérées par les conseillers déjà en cours")
    case ImportOffresEnDifficulteGereesParConseillerDone =>
      log.info("Intégration des offres en difficulté gérées par les conseillers terminée")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des offres en difficulté gérées par les conseillers")
      context.unbecome()
  }

}
