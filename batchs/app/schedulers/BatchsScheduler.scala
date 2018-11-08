package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      candidatsMrsValideesActor: ActorRef,
                      habiletesMRSActor: ActorRef,
                      alerteMailRecruteurActor: ActorRef) {

  import AlerteMailRecruteurActor._
  import CandidatsMRSValideesActor._
  import HabiletesMRSActor._

  val scheduler = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportCandidatsMRSValidees", candidatsMrsValideesActor, StartImportCandidatsMRSValidees)
    scheduler.schedule("ImportHabiletesMRS", habiletesMRSActor, StartImportHabiletesMRS)

    scheduler.schedule("AlerteMailQuotidiennesRecruteurs", alerteMailRecruteurActor, EnvoyerAlertesQuotidiennes)
    scheduler.schedule("AlerteMailHebdomadairesRecruteurs", alerteMailRecruteurActor, EnvoyerAlertesHebdomadaires)
  }
}
