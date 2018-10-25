package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      mrsValideesActor: ActorRef,
                      alerteMailRecruteurActor: ActorRef) {

  import AlerteMailRecruteurActor._
  import MRSValideesActor._

  val scheduler = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportMRSValidees", mrsValideesActor, StartImportMRSValidees)

    scheduler.schedule("AlerteMailQuotidiennesRecruteurs", alerteMailRecruteurActor, EnvoyerAlertesQuotidiennes)
    scheduler.schedule("AlerteMailHebdomadairesRecruteurs", alerteMailRecruteurActor, EnvoyerAlertesHebdomadaires)
  }
}
