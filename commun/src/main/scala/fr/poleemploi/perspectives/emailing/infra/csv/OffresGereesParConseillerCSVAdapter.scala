package fr.poleemploi.perspectives.emailing.infra.csv

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParConseiller
import fr.poleemploi.perspectives.offre.domain.OffreId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Récupère les offres (dites avec préselection) gérées par les conseillers issues d'un fichier CSV
  */
class OffresGereesParConseillerCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  def load(source: Source[ByteString, _]): Future[Stream[OffreGereeParConseiller]] =
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get("kc_offre").exists(_.nonEmpty) &&
          m.get("code_postal").exists(CodePostal.from(_).isDefined) &&
          m.get("mail_suivi").exists(_.nonEmpty) &&
          m.get("enseigne").exists(_.nonEmpty) &&
          m.get("dc_rome_id").exists(_.nonEmpty) &&
          m.get("intitule").exists(_.nonEmpty) &&
          m.get("lieu_de_travail").exists(_.nonEmpty) &&
          m.get("dd_datecreationreport").exists(_.nonEmpty)
      )
      .map(data =>
        OffreGereeParConseiller(
          offreId = OffreId(data("kc_offre")),
          enseigne = data("enseigne"),
          emailCorrespondant = Email(data("mail_suivi")),
          codePostal = CodePostal(data("code_postal")),
          codeROME = CodeROME(data("dc_rome_id")),
          intitule = data("intitule"),
          lieuTravail = data("lieu_de_travail"),
          datePublication = data.get("dd_datecreationreport").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get
        )
      )
      .runWith(Sink.collection)
}
