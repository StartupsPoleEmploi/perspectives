package conf

import candidat.activite.domain._
import candidat.activite.infra.local._
import candidat.activite.infra.mailjet._
import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.localisation.infra.local.LocalisationLocalAdapter
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.LocalisationWSAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ImportHabiletesMRS, ImportMRSDHAE}
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.ImportHabiletesMRSCsvAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ImportHabiletesMRSLocalAdapter, ImportMRSDHAELocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSDHAEPEConnectAdapter
import fr.poleemploi.perspectives.emailing.domain.ImportProspectService
import fr.poleemploi.perspectives.emailing.infra.local.LocalImportProspectService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetImportProspectService
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.offre.infra.local.ReferentielOffreLocalAdapter
import fr.poleemploi.perspectives.prospect.domain.ReferentielProspectCandidat
import fr.poleemploi.perspectives.prospect.infra.local.ReferentielProspectCandidatLocalAdapter
import fr.poleemploi.perspectives.prospect.infra.sql.ReferentielProspectCandidatSqlAdapter

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
  def importOffresGereesParRecruteurService(mailjetImportOffresGereesParRecruteurService: Provider[MailjetImportOffresGereesParRecruteurService],
                                            localImportOffresGereesParRecruteurService: Provider[LocalImportOffresGereesParRecruteurService],
                                            batchsConfig: BatchsConfig): ImportOffresGereesParRecruteurService =
    if (batchsConfig.useMailjet)
      mailjetImportOffresGereesParRecruteurService.get()
    else
      localImportOffresGereesParRecruteurService.get()

  @Provides
  @Singleton
  def importOffresEnDifficulteGereesParRecruteurService(mailjetImportOffresEnDifficulteGereesParRecruteurService: Provider[MailjetImportOffresEnDifficulteGereesParRecruteurService],
                                                        localImportOffresEnDifficulteGereesParRecruteurService: Provider[LocalImportOffresEnDifficulteGereesParRecruteurService],
                                                        batchsConfig: BatchsConfig): ImportOffresEnDifficulteGereesParRecruteurService =
    if (batchsConfig.useMailjet)
      mailjetImportOffresEnDifficulteGereesParRecruteurService.get()
    else
      localImportOffresEnDifficulteGereesParRecruteurService.get()

  @Provides
  @Singleton
  def importOffresGereesParConseillerService(mailjetImportOffresGereesParConseillerService: Provider[MailjetImportOffresGereesParConseillerService],
                                            localImportOffresGereesParConseillerService: Provider[LocalImportOffresGereesParConseillerService],
                                            batchsConfig: BatchsConfig): ImportOffresGereesParConseillerService =
    if (batchsConfig.useMailjet)
      mailjetImportOffresGereesParConseillerService.get()
    else
      localImportOffresGereesParConseillerService.get()

  @Provides
  @Singleton
  def importOffresEnDifficulteGereesParConseillerService(mailjetImportOffresEnDifficulteGereesParConseillerService: Provider[MailjetImportOffresEnDifficulteGereesParConseillerService],
                                                         localImportOffresEnDifficulteGereesParConseillerService: Provider[LocalImportOffresEnDifficulteGereesParConseillerService],
                                                         batchsConfig: BatchsConfig): ImportOffresEnDifficulteGereesParConseillerService =
    if (batchsConfig.useMailjet)
      mailjetImportOffresEnDifficulteGereesParConseillerService.get()
    else
      localImportOffresEnDifficulteGereesParConseillerService.get()

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
  def emailingCandidatsJVRService(mailjetEmailingCandidatsJVRService: Provider[MailjetEmailingCandidatsJVRService],
                                  localEmailingCandidatsJVRService: Provider[LocalEmailingCandidatsJVRService],
                                  batchsConfig: BatchsConfig): EmailingCandidatsJVRService =
    if (batchsConfig.useMailjet)
      mailjetEmailingCandidatsJVRService.get()
    else
      localEmailingCandidatsJVRService.get()

  // on met un fake referentiel offres car on ne s'en sert pas dans les batchs
  @Provides
  def referentielOffre: ReferentielOffre =
    new ReferentielOffreLocalAdapter

  @Provides
  @Singleton
  def referentielProspectCandidat(referentielProspectCandidatSqlAdapter: Provider[ReferentielProspectCandidatSqlAdapter],
                                  referentielProspectCandidatLocalAdapater: Provider[ReferentielProspectCandidatLocalAdapter],
                                  batchsConfig: BatchsConfig): ReferentielProspectCandidat =
    if (batchsConfig.useReferentielProspectCandidat)
      referentielProspectCandidatSqlAdapter.get()
    else
      referentielProspectCandidatLocalAdapater.get()

  @Provides
  @Singleton
  def localisationService(localisationWSAdapter: Provider[LocalisationWSAdapter],
                          localisationLocalAdapter: Provider[LocalisationLocalAdapter],
                          batchsConfig: BatchsConfig): LocalisationService =
    if (batchsConfig.useLocalisation)
      localisationWSAdapter.get()
    else
      localisationLocalAdapter.get()
}
