name := "perspectives-webapp"

libraryDependencies ++= Seq(
  guice,
  ws,
  Dependencies.akkaLogging,
  Dependencies.akkaQuartzScheduler,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus
)