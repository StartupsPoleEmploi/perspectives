package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import candidat.activite.domain.ImportOffresGereesParConseillerService
import schedulers.ImportOffresGereesParConseillerActor.{ImportOffresGereesParConseillerDone, StartImportOffresGereesParConseiller}

import scala.concurrent.ExecutionContextExecutor

object ImportOffresGereesParConseillerActor {

  final val name = "ImportOffresGereesParConseillerActor"

  case object StartImportOffresGereesParConseiller

  case object ImportOffresGereesParConseillerDone

  def props(importOffresGereesParConseillerService: ImportOffresGereesParConseillerService): Props =
    Props(new ImportOffresGereesParConseillerActor(
      importOffresGereesParConseillerService = importOffresGereesParConseillerService
    ))
}

class ImportOffresGereesParConseillerActor(importOffresGereesParConseillerService: ImportOffresGereesParConseillerService) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportOffresGereesParConseiller =>
      log.info("Intégration des offres gérées par les conseillers")
      importOffresGereesParConseillerService.importerOffresGereesParConseiller.map(_ => ImportOffresGereesParConseillerDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportOffresGereesParConseiller =>
      log.warning("Import des offres gérées par les conseillers déjà en cours")
    case ImportOffresGereesParConseillerDone =>
      log.info("Intégration des offres gérées par les conseillers terminée")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des offres gérées par les conseillers")
      context.unbecome()
  }

}
