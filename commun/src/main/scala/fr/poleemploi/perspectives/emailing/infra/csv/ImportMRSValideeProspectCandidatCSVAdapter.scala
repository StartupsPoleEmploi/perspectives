package fr.poleemploi.perspectives.emailing.infra.csv

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import fr.poleemploi.perspectives.emailing.domain.MRSValideeProspectCandidat
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.Future

class ImportMRSValideeProspectCandidatCSVAdapter(override val config: ImportFileAdapterConfig,
                                                 actorSystem: ActorSystem,
                                                 mrsValideeProspectCandidatCSVAdapter: MRSValideeProspectCandidatCSVAdapter) extends ImportFileAdapter[MRSValideeProspectCandidat] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_de_mrs_valides_delta_*.bz2"

  override def integrerFichier(fichier: Path): Future[Stream[MRSValideeProspectCandidat]] =
    mrsValideeProspectCandidatCSVAdapter.load(
      StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
    )

  def importerProspectsCandidats: Future[Stream[MRSValideeProspectCandidat]] = integrerFichiers
}
