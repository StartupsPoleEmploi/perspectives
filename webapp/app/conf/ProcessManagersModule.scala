package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.eventstore.EventStoreListener
import fr.poleemploi.perspectives.candidat.CandidatCommandHandler
import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValideeProcessManager, ReferentielMRS}
import fr.poleemploi.perspectives.commun.infra.peconnect.ws.PEConnectWSAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.{PEConnectAccessTokenStorage, PEConnectCandidatProcessManager}
import net.codingwell.scalaguice.ScalaModule

class RegisterProcessManagers @Inject()(config: WebAppConfig,
                                        eventStoreListener: EventStoreListener,
                                        mrsValideeProcessProcessManager: MRSValideeProcessManager,
                                        peConnectCandidatProcessManager: PEConnectCandidatProcessManager) {
  eventStoreListener.subscribe(mrsValideeProcessProcessManager)

  if (config.usePEConnect) {
    eventStoreListener.subscribe(peConnectCandidatProcessManager)
  }
}

class ProcessManagersModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind(classOf[RegisterProcessManagers]).asEagerSingleton()
  }

  @Provides
  @Singleton
  def mrsValideeProcessManager(candidatCommandHandler: CandidatCommandHandler,
                               referentielMRS: ReferentielMRS): MRSValideeProcessManager =
    new MRSValideeProcessManager(
      candidatCommandHandler = candidatCommandHandler,
      referentielMRS = referentielMRS
    )

  @Provides
  @Singleton
  def peConnectCandidatProcessManager(candidatCommandHandler: CandidatCommandHandler,
                                      peConnectAccessTokenStorage: PEConnectAccessTokenStorage,
                                      peConnectWSAdapter: PEConnectWSAdapter): PEConnectCandidatProcessManager =
    new PEConnectCandidatProcessManager(
      candidatCommandHandler = candidatCommandHandler,
      peConnectAccessTokenStorage = peConnectAccessTokenStorage,
      peConnectWSAdapter = peConnectWSAdapter
    )
}
