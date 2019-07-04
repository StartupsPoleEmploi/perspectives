package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRSDHAE}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.ImportHabiletesMRSCsvAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocalAdapter, ImportMRSDHAELocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSDHAEPEConnectAdapter
import fr.poleemploi.perspectives.emailing.domain.{EmailingService, ImportProspectService}
import fr.poleemploi.perspectives.emailing.infra.local.{LocalEmailingService, LocalImportProspectService}
import fr.poleemploi.perspectives.emailing.infra.mailjet.{MailjetEmailingService, MailjetImportProspectService}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def importMRSDHAE(importMRSDHAELocalAdapter: Provider[ImportMRSDHAELocalAdapter],
                    importMRSDHAEPEConnectAdapter: Provider[ImportMRSDHAEPEConnectAdapter],
                    batchsConfig: BatchsConfig): ImportMRSDHAE =
    if (batchsConfig.usePEConnect)
      importMRSDHAEPEConnectAdapter.get()
    else
      importMRSDHAELocalAdapter.get()

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
  def importHabiletesMRS(importHabiletesMRSCsvAdapter: Provider[ImportHabiletesMRSCsvAdapter],
                         importHabiletesMRSLocalAdapter: Provider[ImportHabiletesMRSLocalAdapter],
                         batchsConfig: BatchsConfig): ImportHabiletesMRS =
    if (batchsConfig.useImportHabiletesMRS)
      importHabiletesMRSCsvAdapter.get()
    else
      importHabiletesMRSLocalAdapter.get()

  @Provides
  @Singleton
  def importProspectService(mailjetImportProspectService: Provider[MailjetImportProspectService],
                            localImportProspectService: Provider[LocalImportProspectService],
                            batchsConfig: BatchsConfig): ImportProspectService =
    if (batchsConfig.useMailjet)
      mailjetImportProspectService.get()
    else
      localImportProspectService.get()

  @Provides
  @Singleton
  def referentielMetier(referentielMetierWSAdapter: Provider[ReferentielMetierWSAdapter]): ReferentielMetier =
    referentielMetierWSAdapter.get()
}
