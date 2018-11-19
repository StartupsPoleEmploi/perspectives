package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.EventStoreListener
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValideeProcessManager, ReferentielMRSCandidat}
import net.codingwell.scalaguice.ScalaModule

class RegisterProcessManagers @Inject()(eventStoreListener: EventStoreListener,
                                        mrsValideeProcessPEConnectManager: MRSValideeProcessManager) {
  eventStoreListener.subscribe(mrsValideeProcessPEConnectManager)
}

class ProcessManagersModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProcessManagers].asEagerSingleton()
  }

  @Provides
  @Singleton
  def mrsValideeProcessManager(candidatCommandHandler: CandidatCommandHandler,
                               referentielMRSCandidat: ReferentielMRSCandidat): MRSValideeProcessManager =
    new MRSValideeProcessManager(
      candidatCommandHandler = candidatCommandHandler,
      referentielMRSCandidat = referentielMRSCandidat
    )

}
