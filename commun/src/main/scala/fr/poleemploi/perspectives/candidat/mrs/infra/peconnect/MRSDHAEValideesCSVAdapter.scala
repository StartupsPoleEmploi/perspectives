package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.emailing.domain.MRSDHAEValideeProspectCandidat
import fr.poleemploi.perspectives.metier.domain.Metier
import fr.poleemploi.perspectives.emailing.infra.csv.identifiantLocalPattern

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MRSDHAEValideesCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  def load(source: Source[ByteString, _]): Future[Stream[MRSDHAEValideeProspectCandidat]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(
        m => m.get("dc_ididentiteexterne").exists(s => idPEConnectPattern.matcher(s).matches()) &&
          m.get("dc_individu_local").exists(s => identifiantLocalPattern.matcher(s).matches()) &&
          m.get("dc_uniteprescriptrice").exists(s => sitePrescripteurPattern.matcher(s).matches()) &&
          m.get("dd_datedebutprestation").exists(_.nonEmpty) &&
          m.get("kc_action_prestation_id").contains("P50") &&
          m.get("dc_nom").exists(_.nonEmpty) &&
          m.get("dc_prenom").exists(_.nonEmpty) &&
          m.get("dc_sexe_id").exists(_.nonEmpty) &&
          m.get("dd_datedebutprestation").exists(_.nonEmpty) &&
          m.get("dc_codepostal").exists(_.nonEmpty) &&
          m.get("dc_adresseemail").exists(_.nonEmpty) &&
          m.get("dc_rome_id").exists(_.nonEmpty) &&
          m.get("dc_lblrome").exists(_.nonEmpty)
      ).map(data =>
      MRSDHAEValideeProspectCandidat(
        peConnectId = PEConnectId(data("dc_ididentiteexterne")),
        identifiantLocal = IdentifiantLocal(data("dc_individu_local")),
        codeDepartement = CodeDepartement(data("dc_codepostal").take(2)),
        dateEvaluation = data.get("dd_datedebutprestation").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get,
        nom = Nom(data("dc_nom")),
        prenom = Prenom(data("dc_prenom")),
        email = Email(data("dc_adresseemail")),
        genre = Genre.buildFrom(data("dc_sexe_id")),
        metier = Metier(
          codeROME = CodeROME(data("dc_rome_id")),
          label = data("dc_lblrome")
        )
      )
    ).runWith(Sink.collection)
      // Il peut y avoir deux fois la même mrs à des dates différentes: on dédoublonne pour être sûr
      .map(s =>
        s.groupBy(m => (m.peConnectId, m.metier.codeROME)).map(_._2.head).toStream
      )
  }
}
