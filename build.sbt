name := "perspectives"

lazy val eventsourcing = project in file("lib-eventsourcing")

lazy val domain = (project in file("domain"))
  .dependsOn(eventsourcing)

lazy val projections = (project in file("projections"))
  .dependsOn(eventsourcing)

lazy val webapp = (project in file("webapp"))
  .enablePlugins(PlayScala)
  .dependsOn(domain, projections)

lazy val root = (project in file("."))
  .aggregate(domain, eventsourcing, projections, webapp)
