package fr.poleemploi.perspectives.emailing.infra.csv

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.Future

class ImportCandidatsJVRCSVAdapter(override val config: ImportFileAdapterConfig,
                                   actorSystem: ActorSystem,
                                   candidatsJVRCSVAdapter: CandidatsJVRCSVAdapter) extends ImportFileAdapter[CandidatId] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_candidats_jvr_*.bz2"

  override def integrerFichier(fichier: Path): Future[Stream[CandidatId]] =
    candidatsJVRCSVAdapter.load(
      StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
    )

  def importerCandidats: Future[Stream[CandidatId]] = integrerFichiers
}
