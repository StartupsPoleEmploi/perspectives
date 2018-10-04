package conf

import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielMRSCandidat
import net.codingwell.scalaguice.ScalaModule
import schedulers.MRSValideesScheduler

class Scheduled @Inject()(mrsValideesScheduler: MRSValideesScheduler) {

  mrsValideesScheduler.schedule
}

class SchedulersModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[Scheduled].asEagerSingleton()
  }

  @Provides
  @Singleton
  def mrsValideesScheduler(actorSystem: ActorSystem,
                           referentielMRSCandidat: ReferentielMRSCandidat,
                           candidatCommandHandler: CandidatCommandHandler): MRSValideesScheduler =
    new MRSValideesScheduler(
      actorSystem = actorSystem,
      referentielMRSCandidat = referentielMRSCandidat,
      candidatCommandHandler = candidatCommandHandler
    )
}
