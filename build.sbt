name := "perspectives"

lazy val eventsourcing = (project in file("lib-eventsourcing"))
  .settings(Settings.noPublishSettings: _*)

lazy val domain = (project in file("domain"))
  .settings(Settings.noPublishSettings: _*)
  .dependsOn(eventsourcing)

lazy val projections = (project in file("projections"))
  .settings(Settings.noPublishSettings: _*)
  .dependsOn(eventsourcing)
  .dependsOn(domain)

lazy val webapp = (project in file("webapp"))
  .settings(Settings.playPublishSettings: _*)
  .enablePlugins(PlayScala)
  .dependsOn(domain, projections)

lazy val root = (project in file("."))
  .settings(Settings.publishSettings: _*)
  .settings(Settings.noPublishSettings: _*)
  .aggregate(domain, eventsourcing, projections, webapp)
