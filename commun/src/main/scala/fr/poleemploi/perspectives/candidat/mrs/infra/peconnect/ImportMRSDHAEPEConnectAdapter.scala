package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.{Files, Path}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.StreamConverters
import fr.poleemploi.perspectives.candidat.mrs.domain.ImportMRSDHAE
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import fr.poleemploi.perspectives.emailing.domain.MRSDHAEValideeProspectCandidat
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportMRSDHAEPEConnectAdapter(override val config: ImportFileAdapterConfig,
                                    actorSystem: ActorSystem,
                                    mrsDHAEValideesCSVAdapter: MRSDHAEValideesCSVAdapter,
                                    mrsDHAEValideesSqlAdapter: MRSDHAEValideesSqlAdapter)
  extends ImportFileAdapter[MRSDHAEValideeProspectCandidat] with ImportMRSDHAE {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override val pattern: String = "perspectives_dhae_*.bz2"

  override def importerProspectsCandidats: Future[Stream[MRSDHAEValideeProspectCandidat]] =
    integrerFichiers

  override def integrerFichier(fichier: Path): Future[Stream[MRSDHAEValideeProspectCandidat]] =
    for {
      mrsDHAEValidees <- mrsDHAEValideesCSVAdapter.load(
        StreamConverters.fromInputStream(() => new BZip2CompressorInputStream(Files.newInputStream(fichier)))
      )
      mrsDHAEValideesPEConnect = mrsDHAEValidees.map(toMRSDHAEValideePEConnect)
      _ <- mrsDHAEValideesSqlAdapter.ajouter(mrsDHAEValideesPEConnect)
    } yield mrsDHAEValidees

  private def toMRSDHAEValideePEConnect(mrsDHAEValideeProspectCandidat: MRSDHAEValideeProspectCandidat) = MRSDHAEValideePEConnect(
    peConnectId = mrsDHAEValideeProspectCandidat.peConnectId,
    codeROME = mrsDHAEValideeProspectCandidat.metier.codeROME,
    codeDepartement = mrsDHAEValideeProspectCandidat.codeDepartement,
    dateEvaluation = mrsDHAEValideeProspectCandidat.dateEvaluation
  )
}
