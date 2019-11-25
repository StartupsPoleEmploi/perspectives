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

object MigrationCandidatInscritAjoutIdentifiants extends App {

  val database = Database.forURL(
    url = "jdbc:postgresql://localhost:5432/perspectives",
    user = "perspectives",
    password = "changeme",
    driver = "org.postgresql.Driver"
  )

  import EventSourcingPostgresDriver.api._

  val objectMapper: ObjectMapper = EventSourcingObjectMapperBuilder(PerspectivesEventSourcingModule).build()
  implicit val actorSystem: ActorSystem = ActorSystem("MigrationCandidatInscritAjoutIdentifiants")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  // candidats inscrits sans identifiants
  val eventsCandidatsInscritsSansIdentifiants: List[(Int, String, String, String)] =
    Await.result(database.run(
      sql"""SELECT e.id, e.stream_name, cp.peconnect_id, pc.identifiant_local
           FROM events e
           INNER JOIN candidats_peconnect cp ON e.stream_name = cp.candidat_id
           INNER JOIN prospects_candidats pc ON pc.peconnect_id = cp.peconnect_id
           WHERE e.event_type = 'CandidatInscritEvent'""".as[(Int, String, String, String)]).map(_.toList),
      Duration.Inf)

  val pwSQL = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_sans_identifiants.sql"))
  val pwES = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_sans_identifiants.json"))

  for (event <- eventsCandidatsInscritsSansIdentifiants) {
    val eventId = event._1
    val candidatId = event._2
    val peconnectId = event._3
    val identifiantLocal = event._4

    pwSQL.println(s"""UPDATE events SET event_data = event_data || json_build_object('peConnectId', '$peconnectId', 'identifiantLocal', '$identifiantLocal')::jsonb WHERE id = $eventId AND event_type = 'CandidatInscritEvent';""")

    pwES.println(s"""{ "update" : {"_id" : "${candidatId.value}", "_type" : "_doc", "_index" : "candidats"} }""")
    pwES.println(Json.stringify(Json.obj("script" -> Json.obj(
      "source" -> "ctx._source.peconnect_id = params.peconnect_id",
      "lang" -> "painless",
      "params" -> Json.obj("peconnect_id" -> peconnectId)
    ))))
    pwES.println(s"""{ "update" : {"_id" : "${candidatId.value}", "_type" : "_doc", "_index" : "candidats"} }""")
    pwES.println(Json.stringify(Json.obj("script" -> Json.obj(
      "source" -> "ctx._source.identifiant_local = params.identifiant_local",
      "lang" -> "painless",
      "params" -> Json.obj("identifiant_local" -> identifiantLocal)
    ))))
  }
  pwSQL.close()
  pwES.close()
  System.exit(0)
}
