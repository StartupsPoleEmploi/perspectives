package conf

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.projections.candidat.CandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur.alerte.AlerteRecruteurProjection
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers.{AlerteMailRecruteurActor, MRSValideesActor, PerspectivesScheduler}

class Scheduled @Inject()(mrsValideesScheduler: PerspectivesScheduler) {

  mrsValideesScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[MRSValideesActor](MRSValideesActor.name)
    bindActor[AlerteMailRecruteurActor](AlerteMailRecruteurActor.name)

    bind[Scheduled].asEagerSingleton()
  }

  @Provides
  def mrsValideesActor(referentielMRSCandidat: ReferentielMRSCandidat,
                       candidatCommandHandler: CandidatCommandHandler): MRSValideesActor =
    new MRSValideesActor(
      referentielMRSCandidat = referentielMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    )

  @Provides
  def alerteMailRecruteurActor(emailingService: EmailingService,
                               alerteRecruteurProjection: AlerteRecruteurProjection,
                               candidatQueryHandler: CandidatQueryHandler,
                               webAppConfig: WebAppConfig): AlerteMailRecruteurActor =
    new AlerteMailRecruteurActor(
      emailingService = emailingService,
      candidatQueryHandler = candidatQueryHandler,
      alerteRecruteurProjection = alerteRecruteurProjection,
      baseURL = webAppConfig.baseURL
    )

  @Provides
  @Singleton
  def perspectivesScheduler(actorSystem: ActorSystem,
                            @Named(MRSValideesActor.name)
                            mrsValideesActor: ActorRef,
                            @Named(AlerteMailRecruteurActor.name)
                            alerteMailRecruteurActor: ActorRef): PerspectivesScheduler =
    new PerspectivesScheduler(
      actorSystem = actorSystem,
      mrsValideesActor = mrsValideesActor,
      alerteMailRecruteurActor = alerteMailRecruteurActor
    )
}
