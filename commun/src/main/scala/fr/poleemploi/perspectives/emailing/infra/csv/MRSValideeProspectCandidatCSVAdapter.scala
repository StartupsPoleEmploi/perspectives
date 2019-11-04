package fr.poleemploi.perspectives.emailing.infra.csv

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.idPEConnectPattern
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.emailing.domain.MRSValideeProspectCandidat
import fr.poleemploi.perspectives.metier.domain.Metier
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Récupère les MRS validees des candidats issues d'un fichier CSV
  */
class MRSValideeProspectCandidatCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  def load(source: Source[ByteString, _]): Future[Stream[MRSValideeProspectCandidat]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get("id_peconnect").exists(s => s.isEmpty || idPEConnectPattern.matcher(s).matches()) &&
          m.get("identifiant_local").exists(s => identifiantLocalPattern.matcher(s).matches()) &&
          m.get("dc_nom").exists(_.nonEmpty) &&
          m.get("dc_prenom").exists(_.nonEmpty) &&
          m.get("dc_sexe_id").exists(_.nonEmpty) &&
          m.get("dd_daterealisation").exists(_.nonEmpty) &&
          m.get("dc_codepostal").exists(_.nonEmpty) &&
          m.get("dc_adresseemail").exists(_.nonEmpty) &&
          m.get("dc_consentement_mail_id").exists(isConsentementMailValide) &&
          m.get("dc_rome_id").exists(_.nonEmpty) &&
          m.get("dc_lblrome").exists(_.nonEmpty)
      )
      .map(data =>
        MRSValideeProspectCandidat(
          peConnectId = data.get("id_peconnect").filterNot(_.isEmpty).map(PEConnectId),
          identifiantLocal = IdentifiantLocal(data("identifiant_local")),
          nom = Nom(data("dc_nom")),
          prenom = Prenom(data("dc_prenom")),
          email = Email(data("dc_adresseemail")),
          genre = Genre.buildFrom(data("dc_sexe_id")),
          codeDepartement = CodeDepartement(data("dc_codepostal").take(2)),
          metier = Metier(
            codeROME = CodeROME(data("dc_rome_id")),
            label = data("dc_lblrome")
          ),
          dateEvaluation = data.get("dd_daterealisation").map(s => LocalDate.parse(s.take(10), dateTimeFormatter)).get
        )
      )
      .runWith(Sink.collection)
      // Il peut y avoir plusieurs enregistrement par email: on prend la MRS avec la date la plus récente
      .map(_.groupBy(_.email).map(_._2.sortWith((m1, m2) => m1.dateEvaluation.isAfter(m2.dateEvaluation)).head).toStream)
  }

  private def isConsentementMailValide(consentement: String): Boolean =
    consentement == "O"

}
