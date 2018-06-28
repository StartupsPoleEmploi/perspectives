package conf

import com.google.inject.{AbstractModule, Provides, Singleton}
import fr.poleemploi.perspectives.domain.conseiller.{AutorisationService, AutorisationServiceDefaut}

class ServicesModule extends AbstractModule {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def autorisationService(webAppConfig: WebAppConfig): AutorisationService = new AutorisationServiceDefaut(
    admins = webAppConfig.admins
  )
}
