package conf

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRS}
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers._

class Scheduled @Inject()(perspectivesScheduler: BatchsScheduler) {

  perspectivesScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ImportMRSValideesActor](ImportMRSValideesActor.name)
    bindActor[HabiletesMRSActor](HabiletesMRSActor.name)

    bind[Scheduled].asEagerSingleton()
  }

  @Provides
  def importMRSValideesActor(importMRS: ImportMRS): ImportMRSValideesActor =
    new ImportMRSValideesActor(
      importMRS = importMRS
    )

  @Provides
  def habiletesMRSActor(importHabiletesMRS: ImportHabiletesMRS): HabiletesMRSActor =
    new HabiletesMRSActor(
      importHabiletesMRS = importHabiletesMRS
    )

  @Provides
  @Singleton
  def perspectivesScheduler(actorSystem: ActorSystem,
                            @Named(ImportMRSValideesActor.name)
                            importMRSValideesActor: ActorRef,
                            @Named(HabiletesMRSActor.name)
                            habiletesMRSActor: ActorRef): BatchsScheduler =
    new BatchsScheduler(
      actorSystem = actorSystem,
      importMRSValideesActor = importMRSValideesActor,
      habiletesMRSActor = habiletesMRSActor
    )
}
