package schedulers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

class BatchsScheduler(actorSystem: ActorSystem,
                      importHabiletesMRSActor: ActorRef,
                      importProspectsCandidatsActor: ActorRef,
                      emailingDisponibilitesCandidatActor: ActorRef) {

  import ImportHabiletesMRSActor._
  import ImportProspectsCandidatsActor._
  import EmailingDisponibilitesCandidatActor._

  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  def schedule: Date = {
    scheduler.schedule("ImportHabiletesMRS", importHabiletesMRSActor, StartImportHabiletesMRS)
    scheduler.schedule("ImportProspectsCandidats", importProspectsCandidatsActor, StartImportProspectsCandidats)
    scheduler.schedule("EmailingDisponibilitesCandidat", emailingDisponibilitesCandidatActor, StartEmailingDisponibilitesCandidat)
  }
}
