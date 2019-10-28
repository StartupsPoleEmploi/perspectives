package fr.poleemploi.perspectives.emailing.infra.csv

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParConseiller
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.Future

class ImportOffresGereesParConseillerCSVAdapter(override val config: ImportFileAdapterConfig,
                                               actorSystem: ActorSystem,
                                               offresGereesParConseillerCSVAdapter: OffresGereesParConseillerCSVAdapter) extends ImportFileAdapter[OffreGereeParConseiller] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_offres_avec_preselection_delta_*.bz2"

  override def integrerFichier(fichier: Path): Future[Stream[OffreGereeParConseiller]] =
    offresGereesParConseillerCSVAdapter.load(
      StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
    )

  def importerOffres: Future[Stream[OffreGereeParConseiller]] = integrerFichiers
}
