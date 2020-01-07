package fr.poleemploi.perspectives.candidat.localisation.infra.ws

import java.io.File
import java.nio.file.Files

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.{CsvParsing, CsvToMap}
import akka.stream.scaladsl.{FileIO, Sink, Source, StreamConverters}
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.Coordonnees
import fr.poleemploi.perspectives.commun.infra.file.FileUtils
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{DataPart, FilePart}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocalisationWSMapping(actorSystem: ActorSystem) {

  import LocalisationWSMapping._

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  def buildCoordonnees(geometryCoordinates: GeometryCoordinates) =
    Coordonnees(
      latitude = geometryCoordinates.latitude,
      longitude = geometryCoordinates.longitude
    )

  def buildLocalisationBulkRequest(villes: Seq[String]): (Source[MultipartFormData.Part[Source[ByteString, _]], NotUsed], File) = {
    val csvFile = buildLocalisationCsvFileRequest(villes)
    (Source(
      FilePart("data", "search.csv", None, FileIO.fromPath(csvFile.toPath)) :: DataPart(
        "columns",
        CITY_COLUMN_NAME
      ) :: List()
    ), csvFile)
  }

  def extractLocalisationBulkResponse(response: WSResponse): Future[Map[String, Coordonnees]] = {
    val csvResponse = FileUtils.createTempFile(response.bodyAsBytes.utf8String, "response.csv")
    StreamConverters
      .fromInputStream(() => Files.newInputStream(csvResponse.toPath))
      .via(CsvParsing.lineScanner(delimiter = ','))
      .via(CsvToMap.toMapAsStrings())
      .filter(m =>
        m.get(CITY_COLUMN_NAME).exists(_.nonEmpty) &&
          m.get("latitude").exists(_.nonEmpty) &&
          m.get("longitude").exists(_.nonEmpty)
      )
      .map(data =>
        data(CITY_COLUMN_NAME) -> Coordonnees(data("latitude").toDouble, data("longitude").toDouble)
      )
      .runWith(Sink.collection)
      .map(_.toMap)
  }

  private def buildLocalisationCsvFileRequest(villes: Seq[String]): File = {
    val content = villes.toSet.mkString("\n")
    FileUtils.createTempFile(s"$CITY_COLUMN_NAME\n$content", "search.csv")
  }
}

object LocalisationWSMapping {
  private val CITY_COLUMN_NAME = "city"
}

case class GeometryCoordinates(longitude: Double,
                               latitude: Double)

object GeometryCoordinates {

  implicit val reads: Reads[GeometryCoordinates] = (
    (JsPath \ 0).read[Double] and
      (JsPath \ 1).read[Double]
    ) (GeometryCoordinates.apply _)
}
