package conf

import com.google.inject._
import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.eventsourcing.eventstore.EventStoreListener
import fr.poleemploi.perspectives.candidat.cv.domain.CVService
import fr.poleemploi.perspectives.candidat.mrs.domain.{ReferentielHabiletesMRS, ReferentielMRSCandidat}
import fr.poleemploi.perspectives.emailing.domain.EmailingService
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.CandidatProjectionElasticsearchAdapter
import fr.poleemploi.perspectives.projections.candidat.infra.local.CandidatNotificationLocalAdapter
import fr.poleemploi.perspectives.projections.candidat.infra.slack.CandidatNotificationSlackAdapter
import fr.poleemploi.perspectives.projections.emailing.{CandidatEmailProjection, RecruteurEmailProjection}
import fr.poleemploi.perspectives.projections.rechercheCandidat.RechercheCandidatQueryHandler
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql.AlerteRecruteurSqlAdapter
import fr.poleemploi.perspectives.projections.recruteur.alerte.{AlerteRecruteurProjection, AlertesRecruteurQuery}
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.Future

class RegisterProjections @Inject()(eventStoreListener: EventStoreListener,
                                    candidatProjection: CandidatProjection,
                                    candidatNotificationProjection: CandidatNotificationProjection,
                                    candidatMailProjection: CandidatEmailProjection,
                                    recruteurProjection: RecruteurProjection,
                                    recruteurEmailProjection: RecruteurEmailProjection,
                                    alerteRecruteurProjection: AlerteRecruteurProjection) {
  eventStoreListener.subscribe(candidatProjection, candidatMailProjection, candidatNotificationProjection)
  eventStoreListener.subscribe(recruteurProjection, recruteurEmailProjection, alerteRecruteurProjection)
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
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
                           referentielMRSCandidat: ReferentielMRSCandidat,
                           referentielMetier: ReferentielMetier,
                           referentielHabiletesMRS: ReferentielHabiletesMRS): CandidatQueryHandler =
    new CandidatQueryHandler(
      candidatProjection = candidatProjection,
      recruteurProjection = recruteurProjection,
      cvService = cvService,
      referentielMRSCandidat = referentielMRSCandidat,
      referentielMetier = referentielMetier,
      referentielHabiletesMRS = referentielHabiletesMRS
    )

  @Provides
  @Singleton
  def candidatEmailProjection(emailingService: EmailingService): CandidatEmailProjection =
    new CandidatEmailProjection(
      emailingService = emailingService
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
  def recruteurQueryHandler(recruteurProjection: RecruteurProjection,
                            alerteRecruteurProjection: AlerteRecruteurProjection): RecruteurQueryHandler =
    new RecruteurQueryHandler {
      override def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]] = {
        case q: TypeRecruteurQuery => recruteurProjection.typeRecruteur(q)
        case q: ProfilRecruteurQuery => recruteurProjection.profilRecruteur(q)
        case q: RecruteursPourConseillerQuery => recruteurProjection.listerPourConseiller(q)
        case q: AlertesRecruteurQuery => alerteRecruteurProjection.alertesParRecruteur(q)
      }
    }

  @Provides
  @Singleton
  def rechercheCandidatQueryHandler(rechercheCandidatService: RechercheCandidatService): RechercheCandidatQueryHandler =
    new RechercheCandidatQueryHandler(
      rechercheCandidatService = rechercheCandidatService
    )

  @Provides
  @Singleton
  def alerteRecruteurProjection(alerteRecruteurSqlAdapter: AlerteRecruteurSqlAdapter): AlerteRecruteurProjection =
    new AlerteRecruteurProjection(
      adapter = alerteRecruteurSqlAdapter
    )

}
