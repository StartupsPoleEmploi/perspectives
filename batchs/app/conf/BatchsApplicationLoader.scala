package conf

import play.api.ApplicationLoader
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader}

class BatchsApplicationLoader extends GuiceApplicationLoader() {

  override def builder(context: ApplicationLoader.Context): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .in(context.environment)
      .loadConfig(context.initialConfiguration)
      .overrides(overrides(context): _*)
      .bindings(
        new InfraModule(),
        new EventSourcingModule(),
        new ProjectionsModule(),
        new SchedulersModule(),
        new ServicesModule()
      )
}
