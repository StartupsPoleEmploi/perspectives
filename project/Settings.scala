import sbt.Keys._
import sbt._
import sbtbuildinfo.{BuildInfoKey, BuildInfoKeys}

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

  // Configuration sans publication
  val noPublishSettings: Seq[Setting[_]] = Seq(
    skip in publish := true
  )

  // Configuration spécifique de la publication pour une appli Play!
  val playSettings: Seq[Setting[_]] = Seq(
    sources in (Compile, doc) := Seq.empty
  )
}
