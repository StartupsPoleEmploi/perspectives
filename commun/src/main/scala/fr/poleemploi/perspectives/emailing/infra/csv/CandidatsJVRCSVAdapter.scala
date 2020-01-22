package fr.poleemploi.perspectives.emailing.infra.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.CandidatId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Récupère les candidats de test du projet Je Veux Recruter - Action Recrut issus d'un fichier CSV
  */
class CandidatsJVRCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  def load(source: Source[ByteString, _]): Future[Stream[CandidatId]] =
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get("candidat_id").exists(_.nonEmpty)
      )
      .map(data => CandidatId(data("candidat_id")))
      .runWith(Sink.collection)
}
