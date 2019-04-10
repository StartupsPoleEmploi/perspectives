package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.dhae.domain.ImportHabiletesDHAE
import fr.poleemploi.perspectives.candidat.dhae.infra.csv.ImportHabiletesDHAECsvAdapter
import fr.poleemploi.perspectives.candidat.dhae.infra.local.ImportHabiletesDHAELocal
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRSCandidat, ReferentielHabiletesMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.ImportHabiletesMRSCsvAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocal, ImportMRSCandidatLocal}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSCandidatPEConnect
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter

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
  def referentielHabiletesMRS(referentielHabiletesMRSSqlAdapter: Provider[ReferentielHabiletesMRSSqlAdapter]): ReferentielHabiletesMRS =
    referentielHabiletesMRSSqlAdapter.get()

  @Provides
  @Singleton
  def importHabiletesMRS(importHabiletesMRSCsvAdapter: Provider[ImportHabiletesMRSCsvAdapter],
                         importHabiletesMRSLocal: Provider[ImportHabiletesMRSLocal],
                         batchsConfig: BatchsConfig): ImportHabiletesMRS =
    if (batchsConfig.useImportHabiletesMRSCsv)
      importHabiletesMRSCsvAdapter.get()
    else
      importHabiletesMRSLocal.get()

  @Provides
  @Singleton
  def importHabiletesDHAE(importHabiletesDHAECsvAdapter: Provider[ImportHabiletesDHAECsvAdapter],
                          importHabiletesDHAELocal: Provider[ImportHabiletesDHAELocal],
                          batchsConfig: BatchsConfig): ImportHabiletesDHAE =
    if (batchsConfig.useImportHabiletesDHAECsv)
      importHabiletesDHAECsvAdapter.get()
    else
      importHabiletesDHAELocal.get()
}
