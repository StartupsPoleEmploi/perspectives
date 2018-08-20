package conf

import com.google.inject.{AbstractModule, Provides, Singleton}
import fr.poleemploi.perspectives.domain.candidat.cv.CVService
import fr.poleemploi.perspectives.domain.candidat.cv.infra.CVBddService
import fr.poleemploi.perspectives.domain.candidat.mrs.ReferentielMRSCandidat
import fr.poleemploi.perspectives.domain.candidat.mrs.infra.{MRSValideeCSVLoader, MRSValideePostgreSql, ReferentielMRSCandidatLocal}
import fr.poleemploi.perspectives.domain.conseiller.{AutorisationService, AutorisationServiceDefaut, ConseillerId}
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def autorisationService(webAppConfig: WebAppConfig): AutorisationService = new AutorisationServiceDefaut(
    admins = webAppConfig.admins.map(ConseillerId)
  )

  @Provides
  @Singleton
  def cvService(database: Database): CVService = new CVBddService(
    database = database,
    driver = PostgresDriver
  )

  @Provides
  @Singleton
  def referentielMetierEvalue(metierEvalueCSVLoader: MRSValideeCSVLoader,
                              postgresqlMetierEvalueService: MRSValideePostgreSql): ReferentielMRSCandidat =
    new ReferentielMRSCandidatLocal(
      mrsValideeCSVLoader = metierEvalueCSVLoader,
      mrsValideesPostgresSql = postgresqlMetierEvalueService
    )
}
