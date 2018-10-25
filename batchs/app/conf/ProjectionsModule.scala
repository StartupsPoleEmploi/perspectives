package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.{EventHandler, EventPublisher}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat.CandidatProjection
import fr.poleemploi.perspectives.projections.candidat.infra.sql.CandidatProjectionSqlAdapter
import fr.poleemploi.perspectives.projections.recruteur.alerte.AlerteRecruteurProjection
import fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql.AlerteRecruteurSqlAdapter
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
import net.codingwell.scalaguice.ScalaModule
import slick.jdbc.JdbcBackend.Database

class RegisterProjections @Inject()(eventPublisher: EventPublisher,
                                    eventHandler: EventHandler,
                                    candidatProjection: CandidatProjection,
                                    alerteRecruteurProjection: AlerteRecruteurProjection) {
  eventPublisher.subscribe(eventHandler)

  eventHandler.subscribe(candidatProjection)
  eventHandler.subscribe(alerteRecruteurProjection)
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def candidatProjectionSqlAdapter(database: Database,
                                   referentielMetier: ReferentielMetier,
                                   rechercheCandidatService: RechercheCandidatService,
                                   batchsConfig: BatchsConfig): CandidatProjectionSqlAdapter =
    new CandidatProjectionSqlAdapter(
      database = database,
      referentielMetier = referentielMetier,
      rechercheCandidatService = rechercheCandidatService,
      candidatsTesteurs = Nil
    )

  @Provides
  @Singleton
  def candidatProjection(candidatProjectionSqlAdapter: CandidatProjectionSqlAdapter): CandidatProjection =
    new CandidatProjection(
      adapter = candidatProjectionSqlAdapter
    )

  @Provides
  def alerteRecruteurSqlAdapter(database: Database,
                                rechercheCandidatService: RechercheCandidatService): AlerteRecruteurSqlAdapter =
    new AlerteRecruteurSqlAdapter(
      database = database,
      rechercheCandidatService = rechercheCandidatService
    )

  @Provides
  @Singleton
  def alerteRecruteurProjection(alerteRecruteurSqlAdapter: AlerteRecruteurSqlAdapter): AlerteRecruteurProjection =
    new AlerteRecruteurProjection(
      alerteRecruteurSqlAdapter = alerteRecruteurSqlAdapter
    )

}

