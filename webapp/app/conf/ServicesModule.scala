package conf

import com.google.inject.{AbstractModule, Provides, Singleton}
import fr.poleemploi.perspectives.domain.candidat.cv.CVService
import fr.poleemploi.perspectives.domain.candidat.cv.infra.CVBddService
import fr.poleemploi.perspectives.domain.conseiller.{AutorisationService, AutorisationServiceDefaut}
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def autorisationService(webAppConfig: WebAppConfig): AutorisationService = new AutorisationServiceDefaut(
    admins = webAppConfig.admins
  )

  @Provides
  @Singleton
  def cvService(database: Database): CVService = new CVBddService(
    database = database,
    driver = PostgresDriver
  )
}
