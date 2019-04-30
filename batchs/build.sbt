name := "perspectives-batchs"

maintainer := "brice.friederich@beta.gouv.fr"

libraryDependencies ++= Seq(
  guice,
  ws,
  Dependencies.akkaLogging,
  Dependencies.akkaQuartzScheduler,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus
)