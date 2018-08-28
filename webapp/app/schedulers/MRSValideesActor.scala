package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.domain.candidat.mrs.ReferentielMRSCandidat

import scala.concurrent.ExecutionContextExecutor

object MRSValideesActor {

  val name: String = this.getClass.getSimpleName

  case object StartImportMRSValidees

  case object ImportMRSValideesDone

  def props(referentielMRSCandidat: ReferentielMRSCandidat): Props =
    Props(new MRSValideesActor(
      referentielMRSCandidat = referentielMRSCandidat
    ))
}

class MRSValideesActor(referentielMRSCandidat: ReferentielMRSCandidat) extends Actor with ActorLogging {

  import MRSValideesActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportMRSValidees =>
      log.info("Intégration des MRS validées pour les candidats")
      referentielMRSCandidat.integrerMRSValidees.map(_ => ImportMRSValideesDone) pipeTo self
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
