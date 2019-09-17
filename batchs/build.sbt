name := "perspectives-batchs"

maintainer := "mickael.barroux@beta.gouv.fr"

libraryDependencies ++= Seq(
  guice,
  ws,
  caffeine,
  Dependencies.akkaLogging,
  Dependencies.akkaQuartzScheduler,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus
)
