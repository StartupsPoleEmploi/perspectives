package fr.poleemploi.perspectives.candidat.activite.infra.csv

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.idPEConnectPattern
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class ActiviteCandidatCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  def load(source: Source[ByteString, _]): Future[Stream[ActiviteCandidatCsv]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(
        m => m.get("dc_ididentiteexterne").exists(s => idPEConnectPattern.matcher(s).matches()) &&
          m.get("nb_heure_travail_decla_actu").exists(x => x.nonEmpty && Try(x.trim.toInt).isSuccess) &&
          m.get("kc_anneeactualisation").exists(x => x.nonEmpty && Try(x.trim.toInt).isSuccess) &&
          m.get("kc_moisactualisation").exists(x => x.nonEmpty && Try(x.trim.toInt).isSuccess) &&
          m.get("kc_anneeactualisation").flatMap(annee =>
            m.get("kc_moisactualisation").map(mois => Try(buildDateActualisation(annee = annee, mois = mois)).isSuccess)
          ).getOrElse(false)
      ).map(data => ActiviteCandidatCsv(
      peConnectId = PEConnectId(data("dc_ididentiteexterne")),
      nom = Nom(data("dc_nom").trim),
      prenom = Prenom(data("dc_prenom").trim),
      nbHeuresTravaillees = Try(data("nb_heure_travail_decla_actu").trim.toInt).getOrElse(0),
      dateActualisation = buildDateActualisation(annee = data("kc_anneeactualisation"), mois = data("kc_moisactualisation"))
    )).runWith(Sink.collection)
      // Il peut y avoir deux fois le meme candidat: on dédoublonne pour être sûr
      .map(s =>
        s.groupBy(m => m.peConnectId).map(_._2.head).toStream
      )
  }

  private def buildDateActualisation(annee: String, mois: String): LocalDate = {
    val year = if (annee.length == 2) s"20$annee" else annee
    LocalDate.of(year.trim.toInt, mois.trim.toInt, 1)
  }
}
