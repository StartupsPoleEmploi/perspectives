package conf

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRSDHAE}
import fr.poleemploi.perspectives.emailing.domain.ImportProspectService
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers._

class Scheduled @Inject()(batchsScheduler: BatchsScheduler) {
  batchsScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ImportMRSDHAEValideesActor](ImportMRSDHAEValideesActor.name)
    bindActor[ImportHabiletesMRSActor](ImportHabiletesMRSActor.name)
    bindActor[ImportProspectsCandidatsActor](ImportProspectsCandidatsActor.name)

    bind(classOf[Scheduled]).asEagerSingleton()
  }

  @Provides
  def importMRSDHAEValideesActor(importMRS: ImportMRSDHAE): ImportMRSDHAEValideesActor =
    new ImportMRSDHAEValideesActor(
      importMRS = importMRS
    )

  @Provides
  def importHabiletesMRSActor(importHabiletesMRS: ImportHabiletesMRS): ImportHabiletesMRSActor =
    new ImportHabiletesMRSActor(
      importHabiletesMRS = importHabiletesMRS
    )

  @Provides
  def importProspectsCandidatsActor(importProspectService: ImportProspectService): ImportProspectsCandidatsActor =
    new ImportProspectsCandidatsActor(
      importProspectService = importProspectService
    )

  @Provides
  @Singleton
  def batchsScheduler(actorSystem: ActorSystem,
                      @Named(ImportMRSDHAEValideesActor.name)
                      importMRSDHAEValideesActor: ActorRef,
                      @Named(ImportHabiletesMRSActor.name)
                      importHabiletesMRSActor: ActorRef,
                      @Named(ImportProspectsCandidatsActor.name)
                      importProspectsCandidatsActor: ActorRef): BatchsScheduler =
    new BatchsScheduler(
      actorSystem = actorSystem,
      importMRSDHAEValideesActor = importMRSDHAEValideesActor,
      importHabiletesMRSActor = importHabiletesMRSActor,
      importProspectsCandidatsActor = importProspectsCandidatsActor
    )
}
