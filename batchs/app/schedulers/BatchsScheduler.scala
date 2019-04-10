package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      candidatsMrsValideesActor: ActorRef,
                      habiletesMRSActor: ActorRef,
                      habiletesDHAEActor: ActorRef) {

  import CandidatsMRSValideesActor._
  import HabiletesDHAEActor._
  import HabiletesMRSActor._

  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportCandidatsMRSValidees", candidatsMrsValideesActor, StartImportCandidatsMRSValidees)
    scheduler.schedule("ImportHabiletesMRS", habiletesMRSActor, StartImportHabiletesMRS)
    scheduler.schedule("ImportHabiletesDHAE", habiletesDHAEActor, StartImportHabiletesDHAE)
  }
}
