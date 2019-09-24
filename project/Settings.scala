import sbt.Keys._
import sbt._
import sbtbuildinfo.{BuildInfoKey, BuildInfoKeys}
import sbtrelease.ReleasePlugin.autoImport._

object Settings {

  val commonSettings: Seq[Setting[_]] = Seq(
    organization := "fr.poleemploi.perspectives",
    scalaVersion := "2.12.8",
    Keys.scalacOptions ++= Seq("-deprecation")
  )

  val buildInfoSettings: Seq[Setting[_]] = Seq(
    BuildInfoKeys.buildInfoKeys := Seq[BuildInfoKey](version),
    BuildInfoKeys.buildInfoPackage := "fr.poleemploi.perspectives.infra"
  )

  private val runtimeVersion = Def.task {
    val v1 = (version in ThisBuild).value
    val v2 = version.value
    if (releaseUseGlobalVersion.value) v1 else v2
  }

  // Configuration sans publication
  val noPublishSettings: Seq[Setting[_]] = Seq(
    skip in publish := true,
    publishTo := {
      Some(Resolver.mavenLocal)
    },
    // Customization of release commit message in order to skip gitlab CI job when pushing new snapshot version commit
    releaseCommitMessage := s"Setting version to ${runtimeVersion.value}" + (if (runtimeVersion.value.endsWith("-SNAPSHOT")) " [ci skip]" else "")
  )

  // Configuration spécifique de la publication pour une appli Play!
  val playSettings: Seq[Setting[_]] = Seq(
    sources in (Compile, doc) := Seq.empty
  )
}
