package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      importMRSDHAEValideesActor: ActorRef,
                      importHabiletesMRSActor: ActorRef,
                      importProspectsCandidatsActor: ActorRef) {

  import ImportHabiletesMRSActor._
  import ImportMRSDHAEValideesActor._
  import ImportProspectsCandidatsActor._

  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportMRSDHAEValidees", importMRSDHAEValideesActor, StartImportMRSValidees)
    scheduler.schedule("ImportHabiletesMRS", importHabiletesMRSActor, StartImportHabiletesMRS)
    scheduler.schedule("ImportProspectsCandidats", importProspectsCandidatsActor, StartImportProspectsCandidats)
  }
}
