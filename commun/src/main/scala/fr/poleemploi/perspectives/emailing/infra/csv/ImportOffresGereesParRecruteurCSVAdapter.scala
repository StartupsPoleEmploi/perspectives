package fr.poleemploi.perspectives.emailing.infra.csv

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import fr.poleemploi.perspectives.emailing.domain.OffreGereeParRecruteur
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.Future

class ImportOffresGereesParRecruteurCSVAdapter(override val config: ImportFileAdapterConfig,
                                               actorSystem: ActorSystem,
                                               offresGereesParRecruteurCSVAdapter: OffresGereesParRecruteurCSVAdapter) extends ImportFileAdapter[OffreGereeParRecruteur] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_offres_sans_preselection_delta_*.bz2"

  override def integrerFichier(fichier: Path): Future[Stream[OffreGereeParRecruteur]] =
    offresGereesParRecruteurCSVAdapter.load(
      StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
    )

  def importerOffres: Future[Stream[OffreGereeParRecruteur]] = integrerFichiers
}
