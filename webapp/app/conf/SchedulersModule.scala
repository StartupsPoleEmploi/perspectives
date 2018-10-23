package conf

import akka.actor.{ActorRef, ActorSystem}
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat
import javax.inject.Named
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import schedulers.{MRSValideesActor, PerspectivesScheduler}

class Scheduled @Inject()(mrsValideesScheduler: PerspectivesScheduler) {

  mrsValideesScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[MRSValideesActor](MRSValideesActor.name)

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
  @Singleton
  def perspectivesScheduler(actorSystem: ActorSystem,
                            @Named(MRSValideesActor.name)
                            mrsValideesActor: ActorRef): PerspectivesScheduler =
    new PerspectivesScheduler(
      actorSystem = actorSystem,
      mrsValideesActor = mrsValideesActor
    )
}
