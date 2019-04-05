package conf

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.dhae.domain.ImportHabiletesDHAE
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRSCandidat}
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers._

class Scheduled @Inject()(perspectivesScheduler: BatchsScheduler) {

  perspectivesScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[CandidatsMRSValideesActor](CandidatsMRSValideesActor.name)
    bindActor[HabiletesMRSActor](HabiletesMRSActor.name)
    bindActor[HabiletesDHAEActor](HabiletesDHAEActor.name)

    bind[Scheduled].asEagerSingleton()
  }

  @Provides
  def mrsValideesActor(importMRSCandidat: ImportMRSCandidat,
                       candidatCommandHandler: CandidatCommandHandler): CandidatsMRSValideesActor =
    new CandidatsMRSValideesActor(
      importMRSCandidat = importMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    )

  @Provides
  def habiletesMRSActor(importHabiletesMRS: ImportHabiletesMRS): HabiletesMRSActor =
    new HabiletesMRSActor(
      importHabiletesMRS = importHabiletesMRS
    )

  @Provides
  def habiletesDHAEActor(importHabiletesDHAE: ImportHabiletesDHAE): HabiletesDHAEActor =
    new HabiletesDHAEActor(
      importHabiletesDHAE = importHabiletesDHAE
    )

  @Provides
  @Singleton
  def perspectivesScheduler(actorSystem: ActorSystem,
                            @Named(CandidatsMRSValideesActor.name)
                            candidatsMrsValideesActor: ActorRef,
                            @Named(HabiletesMRSActor.name)
                            habiletesMRSActor: ActorRef,
                            @Named(HabiletesDHAEActor.name)
                            habiletesDHAEActor: ActorRef): BatchsScheduler =
    new BatchsScheduler(
      actorSystem = actorSystem,
      candidatsMrsValideesActor = candidatsMrsValideesActor,
      habiletesMRSActor = habiletesMRSActor,
      habiletesDHAEActor = habiletesDHAEActor
    )
}
