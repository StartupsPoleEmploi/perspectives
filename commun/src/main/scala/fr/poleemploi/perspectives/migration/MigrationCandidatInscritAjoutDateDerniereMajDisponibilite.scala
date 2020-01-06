package fr.poleemploi.perspectives.migration

import java.io.{File, PrintWriter}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import play.api.libs.json.Json
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object MigrationCandidatInscritAjoutDateDerniereMajDisponibilite extends App {

  val database = Database.forURL(
    url = "jdbc:postgresql://localhost:5432/perspectives",
    user = "perspectives",
    password = "changeme",
    driver = "org.postgresql.Driver"
  )

  import EventSourcingPostgresDriver.api._

  val objectMapper: ObjectMapper = EventSourcingObjectMapperBuilder(PerspectivesEventSourcingModule).build()
  implicit val actorSystem: ActorSystem = ActorSystem("MigrationCandidatInscritAjoutDateDerniereMajDisponibilite")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  // candidats inscrits (par defaut la date de maj derniere dispo = date d'inscription)
  val eventsCandidatsInscrits: List[(String, String)] =
    Await.result(database.run(
      sql"""SELECT e.stream_name, e.event_data->>'date'
           FROM events e
           WHERE e.event_type = 'CandidatInscritEvent'""".as[(String, String)]).map(_.toList),
      Duration.Inf)

  val pwES = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_sans_date_derniere_maj_disponibilite.json"))

  for (event <- eventsCandidatsInscrits) {
    val candidatId = event._1
    val eventDate = event._2

    pwES.println(s"""{ "update" : {"_id" : "${candidatId.value}", "_type" : "_doc", "_index" : "candidats"} }""")
    pwES.println(Json.stringify(Json.obj("script" -> Json.obj(
      "source" -> "ctx._source.date_derniere_maj_disponibilite = params.date_derniere_maj_disponibilite",
      "lang" -> "painless",
      "params" -> Json.obj("date_derniere_maj_disponibilite" -> eventDate)
    ))))
  }
  pwES.close()

  // candidats qui ont mis a jour leur disponibilite
  val eventsCandidatsDisponibilitesModifiees: List[(String, String)] =
    Await.result(database.run(
      sql"""SELECT e.stream_name, max(e.event_data->>'date')
           FROM events e
           WHERE e.event_type = 'DisponibilitesModifieesEvent' GROUP BY e.stream_name""".as[(String, String)]).map(_.toList),
      Duration.Inf)

  val pwES2 = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_avec_date_derniere_maj_disponibilite.json"))

  for (event <- eventsCandidatsDisponibilitesModifiees) {
    val candidatId = event._1
    val eventDate = event._2

    pwES2.println(s"""{ "update" : {"_id" : "${candidatId.value}", "_type" : "_doc", "_index" : "candidats"} }""")
    pwES2.println(Json.stringify(Json.obj("script" -> Json.obj(
      "source" -> "ctx._source.date_derniere_maj_disponibilite = params.date_derniere_maj_disponibilite",
      "lang" -> "painless",
      "params" -> Json.obj("date_derniere_maj_disponibilite" -> eventDate)
    ))))
  }
  pwES2.close()

  System.exit(0)
}
