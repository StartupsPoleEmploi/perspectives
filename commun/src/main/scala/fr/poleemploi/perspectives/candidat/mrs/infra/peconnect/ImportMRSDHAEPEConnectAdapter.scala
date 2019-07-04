package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSDHAE
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSDHAEPEConnectAdapter(override val config: ImportFileAdapterConfig,
                                    actorSystem: ActorSystem,
                                    mrsDHAEValideesCSVAdapter: MRSDHAEValideesCSVAdapter,
                                    mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter)
  extends ImportFileAdapter[MRSDHAEValideePEConnect] with ImportMRSDHAE {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  /** Intègre les full et les delta */
  val pattern: String = "DHAE_*.csv"

  override def integrerMRSDHAEValidees: Future[Unit] =
    integrerFichiers.map(_ => ())

  override def integrerFichier(fichier: Path): Future[Stream[MRSDHAEValideePEConnect]] =
    for {
      mrsDHAEValidees <- mrsDHAEValideesCSVAdapter.load(FileIO.fromPath(fichier))
      _ <- mrsDHAEValideesSqlAdapter.ajouter(mrsDHAEValidees)
    } yield mrsDHAEValidees
}
