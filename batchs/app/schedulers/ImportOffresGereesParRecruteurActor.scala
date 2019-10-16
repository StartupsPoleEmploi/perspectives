package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import candidat.activite.domain.ImportOffresGereesParRecruteurService
import schedulers.ImportOffresGereesParRecruteurActor.{ImportOffresGereesParRecruteurDone, StartImportOffresGereesParRecruteur}

import scala.concurrent.ExecutionContextExecutor

object ImportOffresGereesParRecruteurActor {

  final val name = "ImportOffresGereesParRecruteurActor"

  case object StartImportOffresGereesParRecruteur

  case object ImportOffresGereesParRecruteurDone

  def props(importOffresGereesParRecruteurService: ImportOffresGereesParRecruteurService): Props =
    Props(new ImportOffresGereesParRecruteurActor(
      importOffresGereesParRecruteurService = importOffresGereesParRecruteurService
    ))
}

class ImportOffresGereesParRecruteurActor(importOffresGereesParRecruteurService: ImportOffresGereesParRecruteurService) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportOffresGereesParRecruteur =>
      log.info("Intégration des offres gérées directement par les recruteurs")
      importOffresGereesParRecruteurService.importerOffresGereesParRecruteur.map(_ => ImportOffresGereesParRecruteurDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportOffresGereesParRecruteur =>
      log.warning("Import des offres gérées directement par les recruteurs déjà en cours")
    case ImportOffresGereesParRecruteurDone =>
      log.info("Intégration des des offres gérées directement par les recruteurs terminée")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des des offres gérées directement par les recruteurs")
      context.unbecome()
  }

}
