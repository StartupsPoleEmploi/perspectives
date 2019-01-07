import sbt.Keys._
import sbt.{Resolver, _}
import sbtbuildinfo.{BuildInfoKey, BuildInfoKeys}

object Settings {

  val commonSettings: Seq[Setting[_]] = Seq(
    organization := "fr.poleemploi.perspectives",
    scalaVersion := "2.12.4",
    Keys.scalacOptions ++= Seq("-deprecation")
  )

  val buildInfoSettings: Seq[Setting[_]] = Seq(
    BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](version),
    BuildInfoKeys.buildInfoPackage := "fr.poleemploi.perspectives.infra"
  )

  // Configuration sans publication
  val noPublishSettings: Seq[Setting[_]] = Seq(
    skip in publish := true
  )

  // Configuration générale de la publication
  val publishSettings: Seq[Setting[_]] = Seq(
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
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in(Compile, packageBin) := false,
    publishArtifact in(Compile, packageDoc) := false
  )
}
