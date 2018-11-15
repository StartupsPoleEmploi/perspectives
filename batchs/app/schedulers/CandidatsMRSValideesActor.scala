package schedulers

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportMRSCandidat, MRSValidee}
import fr.poleemploi.perspectives.candidat.{AjouterMRSValideesCommand, CandidatCommandHandler}

import scala.concurrent.{ExecutionContextExecutor, Future}

object CandidatsMRSValideesActor {

  final val name = "CandidatsMRSValideesActor"

  case object StartImportCandidatsMRSValidees

  case object ImportCandidatsMRSValideesDone

  def props(importMRSCandidat: ImportMRSCandidat,
            candidatCommandHandler: CandidatCommandHandler): Props =
    Props(new CandidatsMRSValideesActor(
      importMRSCandidat = importMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    ))
}

class CandidatsMRSValideesActor(importMRSCandidat: ImportMRSCandidat,
                                candidatCommandHandler: CandidatCommandHandler) extends Actor with ActorLogging {

  import CandidatsMRSValideesActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  override def receive: Receive = {
    case StartImportCandidatsMRSValidees =>
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
                  codeDepartement = m.codeDepartement,
                  dateEvaluation = m.dateEvaluation
                )).toList
            )
          ).recover {
            case t: Throwable =>
              log.error(t, s"Erreur lors de l'import des MRS validées")
          }
        ))
      } yield ImportCandidatsMRSValideesDone) pipeTo self
      context.become(importEnCours)
  }

  def importEnCours: Receive = {
    case StartImportCandidatsMRSValidees =>
      log.warning("Import des MRS validées déjà en cours")
    case ImportCandidatsMRSValideesDone =>
      context.unbecome()
    case Failure(t) =>
      log.error(t, "Erreur lors de l'import des MRS validées")
      context.unbecome()
  }
}