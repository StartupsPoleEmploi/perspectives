package fr.poleemploi.perspectives.emailing.infra.csv

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParRecruteur
import fr.poleemploi.perspectives.offre.domain.OffreId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Récupère les offres gerees directement par les recruteurs issues d'un fichier CSV
  */
class OffresGereesParRecruteurCSVAdapter(val actorSystem: ActorSystem) {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  def load(source: Source[ByteString, _]): Future[Stream[OffreGereeParRecruteur]] = {
    source
      .via(CsvParsing.lineScanner(delimiter = ';'))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get("kc_offre").exists(_.nonEmpty) &&
          m.get("code_postal").exists(CodePostal.from(_).isDefined) &&
          m.get("mail_suivi").exists(_.nonEmpty) &&
          m.get("enseigne").exists(_.nonEmpty) &&
          m.get("nom_prenom_correspondant_offre").exists(_.nonEmpty) &&
          m.get("dc_rome_id").exists(_.nonEmpty) &&
          m.get("intitule").exists(_.nonEmpty) &&
          m.get("lieu_de_travail").exists(_.nonEmpty)
      )
      .map(data =>
        OffreGereeParRecruteur(
          offreId = OffreId(data("kc_offre")),
          enseigne = data("enseigne"),
          emailCorrespondant = Email(data("mail_suivi")),
          nomCorrespondant = data("nom_prenom_correspondant_offre"),
          codePostal = CodePostal(data("code_postal")),
          codeROME = CodeROME(data("dc_rome_id")),
          intitule = data("intitule"),
          lieuTravail = data("lieu_de_travail")
        )
      )
      .runWith(Sink.collection)
  }
}
