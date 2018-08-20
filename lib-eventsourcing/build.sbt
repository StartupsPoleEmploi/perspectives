name := "lib-eventsourcing"

libraryDependencies ++= Seq(
  Dependencies.akkaStream,
  Dependencies.jacksonCore,
  Dependencies.jacksonAnnotations,
  Dependencies.jacksonDatabind,
  Dependencies.jacksonDatatypeJSR310,
  Dependencies.jacksonModuleScala,
  Dependencies.slickPg,
  Dependencies.slickHikariCp,
  Dependencies.scalatestplus,
  Dependencies.mockito
)