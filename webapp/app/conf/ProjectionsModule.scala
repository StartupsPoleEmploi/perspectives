package conf

import com.google.inject._
import fr.poleemploi.eventsourcing.eventstore.EventStoreListener
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.mrs.domain.ReferentielHabiletesMRS
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.CandidatProjectionElasticsearchAdapter
import fr.poleemploi.perspectives.projections.candidat.infra.local.CandidatNotificationLocalAdapter
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackAdapter
import fr.poleemploi.perspectives.projections.conseiller.ConseillerQueryHandler
import fr.poleemploi.perspectives.projections.emailing.{CandidatEmailProjection, RecruteurEmailProjection}
import fr.poleemploi.perspectives.projections.metier.MetierQueryHandler
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.projections.recruteur.infra.local.RecruteurNotificationLocalAdapter
import fr.poleemploi.perspectives.projections.recruteur.infra.slack.RecruteurNotificationSlackAdapter
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
import net.codingwell.scalaguice.ScalaModule

class RegisterProjections @Inject()(eventStoreListener: EventStoreListener,
                                    candidatProjection: CandidatProjection,
                                    candidatNotificationProjection: CandidatNotificationProjection,
                                    candidatMailProjection: CandidatEmailProjection,
                                    recruteurProjection: RecruteurProjection,
                                    recruteurEmailProjection: RecruteurEmailProjection,
                                    recruteurNotificationProjection: RecruteurNotificationProjection) {
  eventStoreListener.subscribe(candidatProjection, candidatMailProjection, candidatNotificationProjection)
  eventStoreListener.subscribe(recruteurProjection, recruteurEmailProjection, recruteurNotificationProjection)
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind(classOf[RegisterProjections]).asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjection(candidatProjectionElasticsearchAdapter: CandidatProjectionElasticsearchAdapter): CandidatProjection =
    candidatProjectionElasticsearchAdapter

  @Provides
  @Singleton
  def candidatNotificationProjection(candidatNotificationSlackAdapter: Provider[CandidatNotificationSlackAdapter],
                                     candidatNotificationLocalAdapter: Provider[CandidatNotificationLocalAdapter],
                                     webAppConfig: WebAppConfig): CandidatNotificationProjection =
    if (webAppConfig.useSlackNotification)
      candidatNotificationSlackAdapter.get()
    else
      candidatNotificationLocalAdapter.get()

  @Provides
  @Singleton
  def candidatQueryHandler(candidatProjection: CandidatProjection,
                           recruteurProjection: RecruteurProjection,
                           cvService: CVService,
                           referentielOffre: ReferentielOffre): CandidatQueryHandler =
    new CandidatQueryHandler(
      candidatProjection = candidatProjection,
      recruteurProjection = recruteurProjection,
      cvService = cvService,
      referentielOffre = referentielOffre
    )

  @Provides
  @Singleton
  def candidatEmailProjection(emailingService: EmailingService,
                              referentielMetier: ReferentielMetier): CandidatEmailProjection =
    new CandidatEmailProjection(
      emailingService = emailingService,
      referentielMetier = referentielMetier
    )

  @Provides
  @Singleton
  def recruteurProjection(recruteurProjectionSqlAdapter: RecruteurProjectionSqlAdapter): RecruteurProjection =
    new RecruteurProjection(
      adapter = recruteurProjectionSqlAdapter
    )

  @Provides
  @Singleton
  def recruteurEmailProjection(emailingService: EmailingService): RecruteurEmailProjection =
    new RecruteurEmailProjection(
      emailingService = emailingService
    )

  @Provides
  @Singleton
  def recruteurNotificationProjection(recruteurNotificationSlackAdapter: Provider[RecruteurNotificationSlackAdapter],
                                      recruteurNotificationLocalAdapter: Provider[RecruteurNotificationLocalAdapter],
                                      webAppConfig: WebAppConfig): RecruteurNotificationProjection =
    if (webAppConfig.useSlackNotification)
      recruteurNotificationSlackAdapter.get()
    else
      recruteurNotificationLocalAdapter.get()

  @Provides
  @Singleton
  def recruteurQueryHandler(recruteurProjection: RecruteurProjection): RecruteurQueryHandler =
    new RecruteurQueryHandler(
      recruteurProjection = recruteurProjection
    )

  @Provides
  @Singleton
  def metierQueryHandler(referentielMetier: ReferentielMetier): MetierQueryHandler =
    new MetierQueryHandler(
      referentielMetier = referentielMetier
    )

  @Provides
  @Singleton
  def conseillerQueryHandler(referentielHabiletesMRS: ReferentielHabiletesMRS): ConseillerQueryHandler =
    new ConseillerQueryHandler(
      referentielHabiletesMRS = referentielHabiletesMRS
    )

}
