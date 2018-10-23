package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class PerspectivesScheduler(actorSystem: ActorSystem,
                            mrsValideesActor: ActorRef) {

  import MRSValideesActor._

  val scheduler = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportMRSValidees", mrsValideesActor, StartImportMRSValidees)
  }
}
