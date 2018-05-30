name := "lib-eventsourcing"

libraryDependencies ++= Seq(
  Dependencies.jacksonCore,
  Dependencies.jacksonAnnotations,
  Dependencies.jacksonDatabind,
  Dependencies.jacksonModuleScala,
  Dependencies.slickPg,
  Dependencies.slickHikariCp,
  Dependencies.scalatestplus
)