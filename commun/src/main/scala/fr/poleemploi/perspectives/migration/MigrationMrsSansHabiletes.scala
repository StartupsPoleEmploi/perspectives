package fr.poleemploi.perspectives.migration

import java.io.{File, PrintWriter}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object MigrationMrsSansHabiletes extends App {

  val database = Database.forURL(
    url = "jdbc:postgresql://localhost:5432/perspectives",
    user = "perspectives",
    password = "changeme",
    driver = "org.postgresql.Driver"
  )

  import EventSourcingPostgresDriver.api._

  val objectMapper: ObjectMapper = EventSourcingObjectMapperBuilder(PerspectivesEventSourcingModule).build()
  implicit val actorSystem: ActorSystem = ActorSystem("MigrationMrsSansHabiletes")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  // candidats inscrits avec une MRS sans habilete
  val eventsMrsSansHabiletes: List[(Int, String)] =
    Await.result(database.run(
      sql"""SELECT e.id, array_to_string(h.habiletes, ',')
           FROM events e
           INNER JOIN mrs_habiletes h ON h.code_rome = e.event_data->>'codeROME'
           WHERE e.event_type = 'MRSAjouteeEvent'
           AND e.event_data->>'codeROME' IN('K1205','I1402','H1504')""".as[(Int, String)]).map(_.toList),
      Duration.Inf)

  val pwSQL = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/mrs_sans_habiletes.sql"))
  val pwES = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/mrs_sans_habiletes.json"))

  for (event <- eventsMrsSansHabiletes) {
    val eventId = event._1
    val habiletes = event._2.split(",").map(x => "'" + x.replaceAll("'", "''") + "'")

    pwSQL.println(s"""UPDATE events SET event_data = event_data || json_build_object('habiletes', json_build_array(${habiletes.mkString(",")}))::jsonb WHERE id = $eventId AND event_type = 'MRSAjouteeEvent';""")

  }
  pwSQL.close()
  pwES.close()
  System.exit(0)
}
