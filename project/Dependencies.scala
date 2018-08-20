import sbt._

object Dependencies {

  // Doit correspondre à la version de Jackson utilisée par Play
  val jacksonVersion = "2.8.11"

  // Doit correspondre à la version de Guice utilisée par Play
  val guiceVersion = "4.1.0"

  // La version de slickPg doit suivre la verson de slick
  val slickVersion = "3.2.3"

  val slickPgVersion = "0.16.1"

  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion

  val jacksonAnnotations = "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion

  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion

  /** For DateTimeModule */
  val jacksonDatatypeJSR310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion

  val jacksonModuleScala = "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % jacksonVersion

  val scalaGuice = "net.codingwell" %% "scala-guice" % guiceVersion

  val slickPg = "com.github.tminglei" %% "slick-pg" % slickPgVersion

  val slickHikariCp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion

  val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "0.18"

  val scalatestplus = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

  val mockito = "org.mockito" % "mockito-core" % "2.18.3" % "test"
}
