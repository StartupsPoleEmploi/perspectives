package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSDHAE
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSDHAEPEConnectAdapter(override val config: ImportFileAdapterConfig,
                                    actorSystem: ActorSystem,
                                    mrsDHAEValideesCSVAdapter: MRSDHAEValideesCSVAdapter,
                                    mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter)
  extends ImportFileAdapter[MRSDHAEValideePEConnect] with ImportMRSDHAE {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_dhae_*.bz2"

  override def integrerMRSDHAEValidees: Future[Unit] =
    integrerFichiers.map(_ => ())

  override def integrerFichier(fichier: Path): Future[Stream[MRSDHAEValideePEConnect]] =
    for {
      mrsDHAEValidees <- mrsDHAEValideesCSVAdapter.load(
        StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
      )
      _ <- mrsDHAEValideesSqlAdapter.ajouter(mrsDHAEValidees)
    } yield mrsDHAEValidees
}
