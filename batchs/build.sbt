name := "perspectives-batchs"

libraryDependencies ++= Seq(
  guice,
  ws,
  Dependencies.akkaLogging,
  Dependencies.akkaQuartzScheduler,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus
)