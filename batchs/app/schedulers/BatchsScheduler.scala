package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      candidatsMrsValideesActor: ActorRef,
                      habiletesMRSActor: ActorRef,
                      habiletesDHAEActor: ActorRef,
                      alerteMailRecruteurActor: ActorRef) {

  import AlerteMailRecruteurActor._
  import CandidatsMRSValideesActor._
  import HabiletesMRSActor._
  import HabiletesDHAEActor._

  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportCandidatsMRSValidees", candidatsMrsValideesActor, StartImportCandidatsMRSValidees)
    scheduler.schedule("ImportHabiletesMRS", habiletesMRSActor, StartImportHabiletesMRS)
    scheduler.schedule("ImportHabiletesDHAE", habiletesDHAEActor, StartImportHabiletesDHAE)

    scheduler.schedule("AlerteMailQuotidiennesRecruteurs", alerteMailRecruteurActor, EnvoyerAlertesQuotidiennes)
    scheduler.schedule("AlerteMailHebdomadairesRecruteurs", alerteMailRecruteurActor, EnvoyerAlertesHebdomadaires)
  }
}
