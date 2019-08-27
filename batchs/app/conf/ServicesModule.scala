package conf

import candidat.activite.domain.EmailingDisponibilitesService
import candidat.activite.infra.local.LocalEmailingDisponibilitesService
import candidat.activite.infra.mailjet.MailjetEmailingDisponibilitesService
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRSDHAE}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.ImportHabiletesMRSCsvAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocalAdapter, ImportMRSDHAELocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSDHAEPEConnectAdapter
import fr.poleemploi.perspectives.emailing.domain.ImportProspectService
import fr.poleemploi.perspectives.emailing.infra.local.LocalImportProspectService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetImportProspectService
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.offre.infra.local.ReferentielOffreLocalAdapter

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
  def emailingDisponibilitesService(mailjetEmailingDisponibilitesService: Provider[MailjetEmailingDisponibilitesService],
                                    localEmailingDisponibilitesService: Provider[LocalEmailingDisponibilitesService],
                                    batchsConfig: BatchsConfig): EmailingDisponibilitesService =
    if (batchsConfig.useMailjet)
      mailjetEmailingDisponibilitesService.get()
    else
      localEmailingDisponibilitesService.get()

  @Provides
  @Singleton
  def cvService(csvSqlAdapter: CVSqlAdapter): CVService =
    csvSqlAdapter

  // on met un fake referentiel offres car on ne s'en sert pas dans les batchs
  @Provides
  def referentielOffre: ReferentielOffre =
    new ReferentielOffreLocalAdapter
}
