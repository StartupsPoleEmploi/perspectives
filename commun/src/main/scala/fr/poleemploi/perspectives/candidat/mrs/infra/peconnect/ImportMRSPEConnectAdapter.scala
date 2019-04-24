package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRS
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSPEConnectAdapter(override val config: ImportMRSPEConnectConfig,
                                actorSystem: ActorSystem,
                                mrsValideesCSVAdapter: MRSValideesCSVAdapter,
                                mrsValideesSqlAdapter: MRSValideesSqlAdapter) extends ImportMRS with ImportFileAdapter[MRSValideePEConnect] {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  /** Intègre les full et les delta */
  val pattern: String = "DE_MRS_VALIDES_*.csv"

  override def integrerMRSValidees: Future[Unit] =
    integrerFichiers.map(_ => ())

  override def integrerFichier(fichier: Path): Future[Stream[MRSValideePEConnect]] =
    for {
      mrsValideesPEConnect <- mrsValideesCSVAdapter.load(FileIO.fromPath(fichier))
      _ <- mrsValideesSqlAdapter.ajouter(mrsValideesPEConnect)
    } yield mrsValideesPEConnect
}
