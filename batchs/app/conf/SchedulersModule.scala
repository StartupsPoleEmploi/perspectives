package conf

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSCandidat
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.projections.candidat.CandidatProjection
import fr.poleemploi.perspectives.projections.recruteur.alerte.AlerteRecruteurProjection
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers.{AlerteMailRecruteurActor, BatchsScheduler, MRSValideesActor}

class Scheduled @Inject()(perspectivesScheduler: BatchsScheduler) {

  perspectivesScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[MRSValideesActor](MRSValideesActor.name)
    bindActor[AlerteMailRecruteurActor](AlerteMailRecruteurActor.name)

    bind[Scheduled].asEagerSingleton()
  }

  @Provides
  def mrsValideesActor(importMRSCandidat: ImportMRSCandidat,
                       candidatCommandHandler: CandidatCommandHandler): MRSValideesActor =
    new MRSValideesActor(
      importMRSCandidat = importMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    )

  @Provides
  def alerteMailRecruteurActor(emailingService: EmailingService,
                               alerteRecruteurProjection: AlerteRecruteurProjection,
                               candidatProjection: CandidatProjection,
                               batchsConfig: BatchsConfig): AlerteMailRecruteurActor =
    new AlerteMailRecruteurActor(
      emailingService = emailingService,
      candidatProjection = candidatProjection,
      alerteRecruteurProjection = alerteRecruteurProjection,
      webappURL = batchsConfig.webappURL
    )

  @Provides
  @Singleton
  def perspectivesScheduler(actorSystem: ActorSystem,
                            @Named(MRSValideesActor.name)
                            mrsValideesActor: ActorRef,
                            @Named(AlerteMailRecruteurActor.name)
                            alerteMailRecruteurActor: ActorRef): BatchsScheduler =
    new BatchsScheduler(
      actorSystem = actorSystem,
      mrsValideesActor = mrsValideesActor,
      alerteMailRecruteurActor = alerteMailRecruteurActor
    )
}
