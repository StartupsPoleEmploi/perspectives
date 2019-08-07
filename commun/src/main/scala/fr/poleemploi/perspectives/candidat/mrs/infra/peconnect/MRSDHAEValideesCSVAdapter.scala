package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MRSDHAEValideesCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  def load(source: Source[ByteString, _]): Future[Stream[MRSDHAEValideePEConnect]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(
        m => m.get("dc_ididentiteexterne").exists(s => idPEConnectPattern.matcher(s).matches()) &&
          m.get("dc_rome_id").exists(_.nonEmpty) &&
          m.get("dc_uniteprescriptrice").exists(s => sitePrescripteurPattern.matcher(s).matches()) &&
          m.get("dd_datedebutprestation").exists(_.nonEmpty) &&
          m.get("kc_action_prestation_id").contains("P50")
      ).map(data =>
      MRSDHAEValideePEConnect(
        peConnectId = PEConnectId(data("dc_ididentiteexterne")),
        codeROME = CodeROME(data("dc_rome_id")),
        codeDepartement = CodeDepartement(data("dc_uniteprescriptrice").take(2)),
        dateEvaluation = data.get("dd_datedebutprestation").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get
      )
    ).runWith(Sink.collection)
      // Il peut y avoir deux fois la même mrs à des dates différentes: on dédoublonne pour être sûr
      .map(s =>
      s.groupBy(m => (m.peConnectId, m.codeROME)).map(_._2.head).toStream
    )
  }
}