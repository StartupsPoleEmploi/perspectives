package conf

import com.google.inject.{AbstractModule, Inject, Provides, Singleton}
import fr.poleemploi.eventsourcing.EventPublisher
import fr.poleemploi.perspectives.projections.EmailInscriptionProjection
import net.codingwell.scalaguice.ScalaModule

class RegisterProjections @Inject()(eventPublisher: EventPublisher,
                                    emailInscriptionProjection: EmailInscriptionProjection) {
  eventPublisher.register(emailInscriptionProjection)
}

class ProjectionsModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[RegisterProjections].asEagerSingleton()
  }

  @Provides
  @Singleton
  def emailInscriptionProjection: EmailInscriptionProjection =
    new EmailInscriptionProjection()
}
