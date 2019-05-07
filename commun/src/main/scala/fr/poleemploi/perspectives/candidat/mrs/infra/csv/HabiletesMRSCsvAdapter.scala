package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.mrs.domain.HabiletesMRS
import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}

import scala.concurrent.Future

class HabiletesMRSCsvAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  def load(source: Source[ByteString, _]): Future[Stream[HabiletesMRS]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ','))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get("codeROME").exists(_.nonEmpty) &&
          m.keys.filter(_.startsWith("habilete")).exists(k => m.get(k).exists(_.nonEmpty))
      ).map(data => {
      val habiletes =
        data.keys.filter(_.startsWith("habilete"))
          .map(k => data.getOrElse(k, ""))
          .filter(_.nonEmpty)
          .map(v => Habilete(v.trim.replaceAll("\\n", "")))
          .toList

      HabiletesMRS(
        codeROME = CodeROME(data("codeROME").trim),
        habiletes = habiletes
      )
    }
    ).runWith(Sink.collection)
  }
}
