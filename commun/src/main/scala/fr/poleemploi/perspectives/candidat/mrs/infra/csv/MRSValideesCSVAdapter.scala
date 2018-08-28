package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.mrs.infra.MRSValideeCandidatPEConnect
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.Future

/**
  * Récupère les MRS validees des candidats issues d'un fichier CSV
  */
class MRSValideesCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  def load(source: Source[ByteString, _]): Future[Stream[MRSValideeCandidatPEConnect]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(
        m => m.get("dc_ididentiteexterne").exists(_.nonEmpty) &&
        m.get("dc_commandepresta").exists(_.nonEmpty) &&
        m.get("dd_datecreationbeneficiaire").exists(_.nonEmpty)
      )
      .map(data => MRSValideeCandidatPEConnect(
        peConnectId = PEConnectId(data("dc_ididentiteexterne")),
        codeMetier = data("dc_commandepresta"),
        dateEvaluation = data.get("dd_datecreationbeneficiaire").map(s => LocalDate.parse(s.take(10))).get
      ))
      .runWith(Sink.collection)
  }
}
