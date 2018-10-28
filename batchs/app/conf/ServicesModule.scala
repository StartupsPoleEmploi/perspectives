package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSCandidat
import fr.poleemploi.perspectives.candidat.mrs.infra.local.ImportMRSCandidatLocal
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSCandidatPEConnect
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def importMRSCandidat(importMRSCandidatLocal: Provider[ImportMRSCandidatLocal],
                        importMRSCandidatPEConnectAdapter: Provider[ImportMRSCandidatPEConnect],
                        batchsConfig: BatchsConfig): ImportMRSCandidat =
    if (batchsConfig.usePEConnect)
      importMRSCandidatPEConnectAdapter.get()
    else
      importMRSCandidatLocal.get()

  @Provides
  @Singleton
  def emailingService(mailjetEmailingService: Provider[MailjetEmailingService],
                      localEmailingService: Provider[LocalEmailingService],
                      batchsConfig: BatchsConfig): EmailingService =
    if (batchsConfig.useMailjet)
      mailjetEmailingService.get()
    else
      localEmailingService.get()

  @Provides
  @Singleton
  def referentielMetier(referentielMetierWSAdapter: Provider[ReferentielMetierWSAdapter]): ReferentielMetier =
    referentielMetierWSAdapter.get()

  @Provides
  @Singleton
  def rechercheCandidatService: RechercheCandidatService =
    new RechercheCandidatService
}
