package fr.poleemploi.perspectives.migration

import java.io.{File, PrintWriter}
import java.nio.file.{Path, Paths}
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}
import com.fasterxml.jackson.databind.ObjectMapper
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingObjectMapperBuilder
import fr.poleemploi.eventsourcing.infra.postgresql.{PostgresDriver => EventSourcingPostgresDriver}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Email}
import fr.poleemploi.perspectives.commun.infra.jackson.PerspectivesEventSourcingModule
import play.api.libs.json.{JsFalse, JsString, Json}
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object MigrationCandidatInscritSansMrs extends App {

  val database = Database.forURL(
    url = "jdbc:postgresql://localhost:5432/perspectives",
    user = "perspectives",
    password = "changeme",
    driver = "org.postgresql.Driver"
  )

  import EventSourcingPostgresDriver.api._

  val objectMapper: ObjectMapper = EventSourcingObjectMapperBuilder(PerspectivesEventSourcingModule).build()
  implicit val actorSystem: ActorSystem = ActorSystem("MigrationCandidatInscritSansMrs")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE
  val prettyDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  val candidatsInscritsSansMrs = Await.result(
    loadCsvFile(Paths.get("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_sans_mrs.csv")),
    Duration.Inf
  )

  val pwSQL = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_sans_mrs.sql"))
  val pwES = new PrintWriter(new File("/Users/micka/dev/projects/perspectives/docker/candidats_inscrits_sans_mrs.json"))

  for (candidatInscritSansMrs <- candidatsInscritsSansMrs) {
    val streamVersion: Int = Await.result(
      database.run(
        sql"""SELECT max(stream_version)
           FROM events
           WHERE stream_name = ${candidatInscritSansMrs.candidatId.value}""".as[Int]).map(_.headOption.getOrElse(0)),
      Duration.Inf
    )

    val habiletesMrs: String =
      Await.result(database.run(
        sql"""SELECT array_to_string(habiletes, ',')
           FROM mrs_habiletes
           WHERE code_rome = ${candidatInscritSansMrs.codeROME.value}""".as[String]).map(_.headOption.getOrElse("")),
        Duration.Inf)

    val habiletes = habiletesMrs.split(",").map(x => "\"" + x.replaceAll("'", "''") + "\"")

    pwSQL.println(s"""INSERT INTO events (stream_version, stream_name, event_data, event_type) VALUES (${streamVersion+1}, '${candidatInscritSansMrs.candidatId.value}', '{"date": "2019-12-09T12:54:39.451Z", "@class": "fr.poleemploi.perspectives.candidat.MRSAjouteeEvent", "isDHAE": false, "codeROME": "${candidatInscritSansMrs.codeROME.value}", "habiletes": [${habiletes.mkString(",")}], "candidatId": "${candidatInscritSansMrs.candidatId.value}", "departement": "${candidatInscritSansMrs.codeDepartement.value}", "dateEvaluation": "${candidatInscritSansMrs.dateEvaluation.format(prettyDateFormatter)}"}', 'MRSAjouteeEvent');""")

    pwES.println(s"""{ "update" : {"_id" : "${candidatInscritSansMrs.candidatId.value}", "_type" : "_doc", "_index" : "candidats"} }""")
    pwES.println(Json.stringify(Json.obj("script" -> Json.obj(
      "source" -> "ctx._source.metiers_valides = params.metiers_valides",
      "lang" -> "painless",
      "params" -> Json.obj("metiers_valides" -> Json.arr(Json.obj(
        "departement" -> candidatInscritSansMrs.codeDepartement.value,
        "is_dhae" -> JsFalse,
        "habiletes" -> habiletesMrs.split(",").map(h => JsString(h)),
        "metier" -> candidatInscritSansMrs.codeROME.value
      )))
    ))))
  }
  pwSQL.close()
  pwES.close()
  System.exit(0)

  private def loadCsvFile(fichier: Path): Future[Stream[CandidatInscritSansMrs]] = {
    FileIO
      .fromPath(fichier)
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get("Email").exists(_.nonEmpty) &&
          m.get("candidat_id").exists(_.nonEmpty) &&
          m.get("departement").exists(_.nonEmpty) &&
          m.get("mrs_code_rome").exists(_.nonEmpty) &&
          m.get("mrs_date").exists(_.nonEmpty)
      ).map(data => CandidatInscritSansMrs(
      email = Email(data("Email")),
      candidatId = CandidatId(data("candidat_id")),
      codeDepartement = CodeDepartement(data("departement")),
      codeROME = CodeROME(data("mrs_code_rome")),
      dateEvaluation = data.get("mrs_date").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get,
    )
    ).runWith(Sink.collection)
  }
}

case class CandidatInscritSansMrs(email: Email,
                                  candidatId: CandidatId,
                                  codeDepartement: CodeDepartement,
                                  codeROME: CodeROME,
                                  dateEvaluation: LocalDate)
