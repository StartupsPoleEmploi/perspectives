import sbt.Keys._
import sbt._

object Settings {

  // Repositories
  /*val customResolvers: Seq[Resolver] = Seq(
    "HTTPS Maven Central" at "https://repo1.maven.org/maven2/",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Typesafe repository mvn" at "http://repo.typesafe.com/typesafe/maven-releases/",
    Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
  )*/

  val settings: Seq[Setting[_]] = Seq(
    organization := "fr.poleemploi",
    scalaVersion := "2.12.4",
    version := "0.1.0-SNAPSHOT"
  )
}
