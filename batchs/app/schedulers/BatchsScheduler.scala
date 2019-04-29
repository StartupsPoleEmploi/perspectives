package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      importMRSValideesActor: ActorRef,
                      habiletesMRSActor: ActorRef) {

  import HabiletesMRSActor._
  import ImportMRSValideesActor._

  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportMRSValidees", importMRSValideesActor, StartImportMRSValidees)
    scheduler.schedule("ImportHabiletesMRS", habiletesMRSActor, StartImportHabiletesMRS)
  }
}
