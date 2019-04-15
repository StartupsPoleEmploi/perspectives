package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.EventStoreListener
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValideeProcessManager, ReferentielMRS}
import net.codingwell.scalaguice.ScalaModule

class RegisterProcessManagers @Inject()(eventStoreListener: EventStoreListener,
                                        mrsValideeProcessProcessManager: MRSValideeProcessManager) {
  eventStoreListener.subscribe(mrsValideeProcessProcessManager)
}

class ProcessManagersModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProcessManagers].asEagerSingleton()
  }

  @Provides
  @Singleton
  def mrsValideeProcessManager(candidatCommandHandler: CandidatCommandHandler,
                               referentielMRS: ReferentielMRS): MRSValideeProcessManager =
    new MRSValideeProcessManager(
      candidatCommandHandler = candidatCommandHandler,
      referentielMRS = referentielMRS
    )

}
