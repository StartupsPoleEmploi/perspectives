package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.candidat.{AjouterMRSValideesCommand, CandidatCommandHandler}

import scala.concurrent.{ExecutionContextExecutor, Future}

object MRSValideesActor {

  val name: String = this.getClass.getSimpleName

  case object StartImportMRSValidees

  case object ImportMRSValideesDone

  def props(referentielMRSCandidat: ReferentielMRSCandidat,
            candidatCommandHandler: CandidatCommandHandler): Props =
    Props(new MRSValideesActor(
      referentielMRSCandidat = referentielMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    ))
}

class MRSValideesActor(referentielMRSCandidat: ReferentielMRSCandidat,
                       candidatCommandHandler: CandidatCommandHandler) extends Actor with ActorLogging {

  import MRSValideesActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportMRSValidees =>
      log.info("Intégration des MRS validées pour les candidats")
      (for {
        mrsValidees <- referentielMRSCandidat.integrerMRSValidees
        _ <- Future.sequence(mrsValidees.groupBy(_.candidatId).map(v =>
          candidatCommandHandler.ajouterMRSValidees(
            AjouterMRSValideesCommand(
              id = v._1,
              mrsValidees = v._2.map(m =>
                MRSValidee(
                  codeROME = m.codeROME,
                  dateEvaluation = m.dateEvaluation
                )).toList
            )
          ).recover {
            case t: Throwable =>
              log.error(t, s"Erreur lors de l'import des MRS validées")
          }
        ))
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
