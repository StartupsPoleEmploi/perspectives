package fr.poleemploi.perspectives.candidat.activite.infra.csv

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportActiviteCandidatCsvAdapter(override val config: ImportFileAdapterConfig,
                                       actorSystem: ActorSystem,
                                       activiteCandidatCSVAdapter: ActiviteCandidatCSVAdapter)
  extends ImportFileAdapter[ActiviteCandidatCsv] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_activite_candidat_*.bz2"

  override def integrerFichier(fichier: Path): Future[Stream[ActiviteCandidatCsv]] =
    activiteCandidatCSVAdapter.load(
      StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
    )

  def importerActivitesCandidats: Future[Stream[ActiviteCandidatCsv]] = integrerFichiers
}
