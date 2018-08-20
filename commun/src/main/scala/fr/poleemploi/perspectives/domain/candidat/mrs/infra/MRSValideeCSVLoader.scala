package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import java.nio.file.Path
import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink}
import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId

import scala.concurrent.Future

/**
  * Récupère les MRS validees des candidats issues d'un fichier CSV
  */
class MRSValideeCSVLoader(directory: Path,
                          implicit val actorSystem: ActorSystem) {

  // FIXME : fichier dans le répertoire respectant un pattern + classés par date
  // FIXME : déplacement après lecture
  //val file: Path = directory.resolve("DE_MRS_VALIDES_FULL_2018-08-05.csv")
  val file: Path = directory.resolve("DE_MRS_VALIDES_DELTA_2018-08-12.csv")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def load: Future[Stream[MRSValideeCandidatPEConnect]] = {
    if (!directory.toFile.exists()) {
      return Future.failed(new RuntimeException(s"Le répertoire $directory n'existe pas"))
    }
    FileIO.fromPath(file)
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(m => m.get("dc_ididentiteexterne").exists(_.nonEmpty) &&
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
