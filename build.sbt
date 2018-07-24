name := "perspectives"

lazy val eventsourcing = (project in file("lib-eventsourcing"))
  .settings(Settings.commonSettings: _*)
  .settings(Settings.noPublishSettings: _*)

lazy val commun = (project in file("commun"))
  .settings(Settings.commonSettings: _*)
  .settings(Settings.noPublishSettings: _*)
  .dependsOn(eventsourcing)

lazy val projections = (project in file("projections"))
  .settings(Settings.commonSettings: _*)
  .settings(Settings.noPublishSettings: _*)
  .dependsOn(eventsourcing)
  .dependsOn(commun)

lazy val webapp = (project in file("webapp"))
  .settings(Settings.commonSettings: _*)
  .settings(Settings.buildInfoSettings: _*)
  .settings(Settings.playPublishSettings: _*)
  .enablePlugins(PlayScala, BuildInfoPlugin)
  .dependsOn(commun, projections)

lazy val root = (project in file("."))
  .settings(Settings.commonSettings: _*)
  .settings(Settings.publishSettings: _*)
  .settings(Settings.noPublishSettings: _*)
  .aggregate(commun, eventsourcing, projections, webapp)
