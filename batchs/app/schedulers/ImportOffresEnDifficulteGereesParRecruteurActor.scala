package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import candidat.activite.domain.ImportOffresEnDifficulteGereesParRecruteurService
import schedulers.ImportOffresEnDifficulteGereesParRecruteurActor.{ImportOffresEnDifficulteGereesParRecruteurDone, StartImportOffresEnDifficulteGereesParRecruteur}

import scala.concurrent.ExecutionContextExecutor

object ImportOffresEnDifficulteGereesParRecruteurActor {

  final val name = "ImportOffresEnDifficulteGereesParRecruteurActor"

  case object StartImportOffresEnDifficulteGereesParRecruteur

  case object ImportOffresEnDifficulteGereesParRecruteurDone

  def props(importOffresEnDifficulteGereesParRecruteurService: ImportOffresEnDifficulteGereesParRecruteurService): Props =
    Props(new ImportOffresEnDifficulteGereesParRecruteurActor(
      importOffresEnDifficulteGereesParRecruteurService = importOffresEnDifficulteGereesParRecruteurService
    ))
}

class ImportOffresEnDifficulteGereesParRecruteurActor(importOffresEnDifficulteGereesParRecruteurService: ImportOffresEnDifficulteGereesParRecruteurService) extends Actor with ActorLogging {

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportOffresEnDifficulteGereesParRecruteur =>
      log.info("Intégration des offres en difficulté gérées directement par les recruteurs")
      importOffresEnDifficulteGereesParRecruteurService.importerOffresEnDifficulteGereesParRecruteur.map(_ => ImportOffresEnDifficulteGereesParRecruteurDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportOffresEnDifficulteGereesParRecruteur =>
      log.warning("Import des offres en difficulté gérées directement par les recruteurs déjà en cours")
    case ImportOffresEnDifficulteGereesParRecruteurDone =>
      log.info("Intégration des des offres en difficulté gérées directement par les recruteurs terminée")
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des des offres en difficulté gérées directement par les recruteurs")
      context.unbecome()
  }

}
