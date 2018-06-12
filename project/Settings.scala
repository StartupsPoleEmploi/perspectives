import sbt.Keys._
import sbt.{Resolver, _}

object Settings {

  val settings: Seq[Setting[_]] = Seq(
    organization := "fr.poleemploi.perspectives",
    scalaVersion := "2.12.4"
  )

  // Configuration sans publication
  val noPublishSettings: Seq[Setting[_]] = settings ++ Seq(
    skip in publish := true
  )

  // Configuration générale de la publication
  val publishSettings: Seq[Setting[_]] = settings ++ Seq(
    publishArtifact in(Compile, packageBin) := true,
    publishArtifact in(Compile, packageDoc) := true,
    publishArtifact in(Compile, packageSrc) := true,
    publishArtifact in(Test, packageBin) := false,
    publishArtifact in(Test, packageDoc) := false,
    publishArtifact in(Test, packageSrc) := false,
    publishMavenStyle := true,
    pomIncludeRepository := { _ => false },
    publishTo := {
      Some(Resolver.mavenLocal)
    }
  )

  // Configuration spécifique de la publication pour une appli Play!
  val playPublishSettings: Seq[Setting[_]] = publishSettings ++ Seq(
    publishArtifact in(Compile, packageBin) := false,
    publishArtifact in(Compile, packageDoc) := false,
    publishArtifact in(Compile, packageSrc) := true
  )
}
