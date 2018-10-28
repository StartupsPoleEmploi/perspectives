package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRSCandidat, MRSValidee}
import fr.poleemploi.perspectives.candidat.{AjouterMRSValideesCommand, CandidatCommandHandler}

import scala.concurrent.{ExecutionContextExecutor, Future}

object MRSValideesActor {

  final val name = "MRSValideesActor"

  case object StartImportMRSValidees

  case object ImportMRSValideesDone

  def props(importMRSCandidat: ImportMRSCandidat,
            candidatCommandHandler: CandidatCommandHandler): Props =
    Props(new MRSValideesActor(
      importMRSCandidat = importMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    ))
}

class MRSValideesActor(importMRSCandidat: ImportMRSCandidat,
                       candidatCommandHandler: CandidatCommandHandler) extends Actor with ActorLogging {

  import MRSValideesActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportMRSValidees =>
      log.info("Intégration des MRS validées pour les candidats")
      (for {
        mrsValidees <- importMRSCandidat.integrerMRSValidees
        _ <- Future.sequence(mrsValidees.groupBy(_.candidatId).map(v =>
          candidatCommandHandler.handle(
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
