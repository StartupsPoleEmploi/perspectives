package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat

class MRSValideesScheduler(actorSystem: ActorSystem,
                           referentielMRSCandidat: ReferentielMRSCandidat,
                           candidatCommandHandler: CandidatCommandHandler) {

  import MRSValideesActor._

  val scheduler = QuartzSchedulerExtension(actorSystem)

  val actor: ActorRef = actorSystem.actorOf(MRSValideesActor.props(referentielMRSCandidat, candidatCommandHandler), MRSValideesActor.name)

  def schedule: Date = {
    scheduler.schedule("ImportMRSValidees", actor, StartImportMRSValidees)
  }
}
