package conf

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.cv.infra.sql.CVSqlAdapter
import fr.poleemploi.perspectives.candidat.mrs.domain.{ReferentielHabiletesMRS, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.candidat.mrs.infra.local.{ReferentielHabiletesMRSLocal, ReferentielMRSCandidatLocal}
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ReferentielMRSCandidatPEConnect
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.conseiller.{AutorisationService, ConseillerId}
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.emailing.infra.local.LocalEmailingService
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetEmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.metier.infra.file.ReferentielMetierFileAdapter
import fr.poleemploi.perspectives.metier.infra.ws.ReferentielMetierWSAdapter
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService
import fr.poleemploi.perspectives.recruteur.commentaire.infra.local.CommentaireServiceLocal
import fr.poleemploi.perspectives.recruteur.commentaire.infra.slack.SlackCommentaireAdapter

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def autorisationService(webAppConfig: WebAppConfig): AutorisationService =
    new AutorisationService(
      admins = webAppConfig.admins.map(ConseillerId)
    )

  @Provides
  @Singleton
  def cvService(csvSqlAdapter: CVSqlAdapter): CVService =
    csvSqlAdapter

  @Provides
  @Singleton
  def referentielMRSCandidat(referentielMRSCandidatPEConnect: Provider[ReferentielMRSCandidatPEConnect],
                             referentielMRSCandidatLocal: Provider[ReferentielMRSCandidatLocal],
                             webAppConfig: WebAppConfig): ReferentielMRSCandidat =
    if (webAppConfig.usePEConnect)
      referentielMRSCandidatPEConnect.get()
    else
      referentielMRSCandidatLocal.get()

  @Provides
  @Singleton
  def referentielHabiletesMRS(referentielHabiletesMRSSqlAdapter: Provider[ReferentielHabiletesMRSSqlAdapter],
                              referentielHabiletesMRSLocal: Provider[ReferentielHabiletesMRSLocal],
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
                        referentielMetierFileAdapter: Provider[ReferentielMetierFileAdapter],
                        webAppConfig: WebAppConfig): ReferentielMetier =
    if (webAppConfig.useReferentielMetier)
      referentielMetierWSAdapter.get()
    else
      referentielMetierFileAdapter.get()

  @Provides
  @Singleton
  def rechercheCandidatService: RechercheCandidatService =
    new RechercheCandidatService

  @Provides
  @Singleton
  def commentaireService(slackCommentaireAdapter: Provider[SlackCommentaireAdapter],
                         commentaireServiceLocal: Provider[CommentaireServiceLocal],
                         webAppConfig: WebAppConfig): CommentaireService =
    if (webAppConfig.useSlackNotification)
      slackCommentaireAdapter.get()
    else
      commentaireServiceLocal.get()
}
