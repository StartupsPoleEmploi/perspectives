package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.dhae.domain.ImportHabiletesDHAE
import fr.poleemploi.perspectives.candidat.dhae.infra.csv.ImportHabiletesDHAECsvAdapter
import fr.poleemploi.perspectives.candidat.dhae.infra.local.ImportHabiletesDHAELocalAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.ImportHabiletesMRSCsvAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocalAdapter, ImportMRSLocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSPEConnectAdapter
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def importMRS(importMRSLocalAdapter: Provider[ImportMRSLocalAdapter],
               importMRSPEConnectAdapter: Provider[ImportMRSPEConnectAdapter],
               batchsConfig: BatchsConfig): ImportMRS =
    if (batchsConfig.usePEConnect)
      importMRSPEConnectAdapter.get()
    else
      importMRSLocalAdapter.get()

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
  def importHabiletesMRS(importHabiletesMRSCsvAdapter: Provider[ImportHabiletesMRSCsvAdapter],
                         importHabiletesMRSLocalAdapter: Provider[ImportHabiletesMRSLocalAdapter],
                         batchsConfig: BatchsConfig): ImportHabiletesMRS =
    if (batchsConfig.useImportHabiletesMRSCsv)
      importHabiletesMRSCsvAdapter.get()
    else
      importHabiletesMRSLocalAdapter.get()

  @Provides
  @Singleton
  def importHabiletesDHAE(importHabiletesDHAECsvAdapter: Provider[ImportHabiletesDHAECsvAdapter],
                          importHabiletesDHAELocalAdapter: Provider[ImportHabiletesDHAELocalAdapter],
                          batchsConfig: BatchsConfig): ImportHabiletesDHAE =
    if (batchsConfig.useImportHabiletesDHAECsv)
      importHabiletesDHAECsvAdapter.get()
    else
      importHabiletesDHAELocalAdapter.get()
}
