package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.candidat.localisation.infra.local.LocalisationLocalAdapter
import fr.poleemploi.perspectives.candidat.localisation.infra.ws.LocalisationWSAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ReferentielHabiletesMRS, ReferentielMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ReferentielHabiletesMRSLocalAdapter, ReferentielMRSLocalAdapter}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSPEConnect
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.geo.domain.ReferentielRegion
import fr.poleemploi.perspectives.commun.geo.infra.local.ReferentielRegionLocalAdapter
import fr.poleemploi.perspectives.commun.geo.infra.ws.ReferentielRegionWSAdapter
import fr.poleemploi.perspectives.conseiller.AutorisationService
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.local.ReferentielMetierLocalAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.offre.infra.local.ReferentielOffreLocalAdapter
import fr.poleemploi.perspectives.offre.infra.ws.ReferentielOffreWSAdapter
import fr.poleemploi.perspectives.rome.domain.ReferentielRome
import fr.poleemploi.perspectives.rome.infra.local.ReferentielRomeLocalAdapter
import fr.poleemploi.perspectives.rome.infra.ws.ReferentielRomeWSAdapter

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def autorisationService(webAppConfig: WebAppConfig): AutorisationService =
    new AutorisationService(
      admins = webAppConfig.conseillersAdmins
    )

  @Provides
  @Singleton
  def referentielMRS(referentielMRSPEConnect: Provider[ReferentielMRSPEConnect],
                     referentielMRSLocal: Provider[ReferentielMRSLocalAdapter],
                     webAppConfig: WebAppConfig): ReferentielMRS =
    if (webAppConfig.usePEConnect)
      referentielMRSPEConnect.get()
    else
      referentielMRSLocal.get()

  @Provides
  @Singleton
  def referentielHabiletesMRS(referentielHabiletesMRSSqlAdapter: Provider[ReferentielHabiletesMRSSqlAdapter],
                              referentielHabiletesMRSLocal: Provider[ReferentielHabiletesMRSLocalAdapter],
                              webAppConfig: WebAppConfig): ReferentielHabiletesMRS =
    if (webAppConfig.useReferentielHabiletesMRS)
      referentielHabiletesMRSSqlAdapter.get()
    else
      referentielHabiletesMRSLocal.get()

  @Provides
  @Singleton
  def emailingService(mailjetEmailingService: Provider[MailjetEmailingService],
                      localEmailingService: Provider[LocalEmailingService],
                      webAppConfig: WebAppConfig): EmailingService =
    if (webAppConfig.useMailjet)
      mailjetEmailingService.get()
    else
      localEmailingService.get()

  @Provides
  @Singleton
  def referentielMetier(referentielMetierWSAdapter: Provider[ReferentielMetierWSAdapter],
                        referentielMetierLocalAdapter: Provider[ReferentielMetierLocalAdapter],
                        webAppConfig: WebAppConfig): ReferentielMetier =
    if (webAppConfig.useReferentielMetier)
      referentielMetierWSAdapter.get()
    else
      referentielMetierLocalAdapter.get()

  @Provides
  @Singleton
  def referentielRome(referentielRomeWSAdapter: Provider[ReferentielRomeWSAdapter],
                      referentielRomeLocalAdapter: Provider[ReferentielRomeLocalAdapter],
                      webAppConfig: WebAppConfig): ReferentielRome =
    if (webAppConfig.useReferentielRome)
      referentielRomeWSAdapter.get()
    else
      referentielRomeLocalAdapter.get()

  @Provides
  @Singleton
  def localisationService(localisationWSAdapter: Provider[LocalisationWSAdapter],
                          localisationLocalAdapter: Provider[LocalisationLocalAdapter],
                          webAppConfig: WebAppConfig): LocalisationService =
    if (webAppConfig.useLocalisation)
      localisationWSAdapter.get()
    else
      localisationLocalAdapter.get()

  @Provides
  @Singleton
  def referentielOffre(referentielOffreWSAdapter: Provider[ReferentielOffreWSAdapter],
                       referentielOffreLocalAdapter: Provider[ReferentielOffreLocalAdapter],
                       webAppConfig: WebAppConfig): ReferentielOffre =
    if (webAppConfig.useReferentielOffre)
      referentielOffreWSAdapter.get()
    else
      referentielOffreLocalAdapter.get()

  @Provides
  @Singleton
  def referentielRegion(referentielRegionWSAdapter: Provider[ReferentielRegionWSAdapter],
                        referentielRegionLocalAdapter: Provider[ReferentielRegionLocalAdapter],
                        webAppConfig: WebAppConfig): ReferentielRegion =
    if (webAppConfig.useReferentielRegion)
      referentielRegionWSAdapter.get()
    else
      referentielRegionLocalAdapter.get()
}
