import sbt._

object Dependencies {

  // Doivent correspondre aux versions utilisées par Play
  val jacksonVersion = "2.9.8"
  val guiceVersion = "4.2.2"
  val akkaVersion = "2.5.22"

  // La version de slickPg doit suivre la version de slick
  val slickVersion = "3.3.0"

  val slickPgVersion = "0.17.2"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaLogging = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion

  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test

  val akkaQuartzScheduler = "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.0-akka-2.5.x"

  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion

  val jacksonAnnotations = "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion

  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion

  /** Pour le DateTimeModule */
  val jacksonDatatypeJSR310 = "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % jacksonVersion

  val jacksonModuleScala = "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % jacksonVersion

  val scalaGuice = "net.codingwell" %% "scala-guice" % guiceVersion

  val slickPg = "com.github.tminglei" %% "slick-pg" % slickPgVersion

  val slickHikariCp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion

  val alpakka = "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "1.0.0"

  val scalatestplus = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

  val mockito = "org.mockito" % "mockito-core" % "2.18.3" % "test"
}
