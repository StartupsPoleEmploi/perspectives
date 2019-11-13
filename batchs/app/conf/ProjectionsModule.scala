package conf

import com.google.inject._
import fr.poleemploi.perspectives.offre.domain.ReferentielOffre
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch.CandidatProjectionElasticsearchQueryAdapter
import fr.poleemploi.perspectives.projections.recruteur._
import fr.poleemploi.perspectives.projections.recruteur.infra.sql.RecruteurProjectionSqlAdapter
import net.codingwell.scalaguice.ScalaModule

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def candidatProjectionQuery(candidatProjectionElasticsearchQueryAdapter: CandidatProjectionElasticsearchQueryAdapter): CandidatProjectionQuery =
    candidatProjectionElasticsearchQueryAdapter

  @Provides
  @Singleton
  def recruteurProjectionQuery(recruteurProjectionSqlAdapter: RecruteurProjectionSqlAdapter): RecruteurProjectionQuery =
    new RecruteurProjectionQuery(
      adapter = recruteurProjectionSqlAdapter
    )

  @Provides
  @Singleton
  def candidatQueryHandler(candidatProjectionQuery: CandidatProjectionQuery,
                           recruteurProjectionQuery: RecruteurProjectionQuery,
                           referentielOffre: ReferentielOffre): CandidatQueryHandler =
    new CandidatQueryHandler(
      candidatProjectionQuery = candidatProjectionQuery,
      recruteurProjectionQuery = recruteurProjectionQuery,
      referentielOffre = referentielOffre
    )
}
