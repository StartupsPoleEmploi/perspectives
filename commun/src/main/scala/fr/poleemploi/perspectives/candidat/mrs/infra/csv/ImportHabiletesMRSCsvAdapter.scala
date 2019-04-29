package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.nio.file._

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.candidat.mrs.domain.{HabiletesMRS, ImportHabiletesMRS}
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportHabiletesMRSCsvAdapter(habiletesMRSCsvAdapter: HabiletesMRSCsvAdapter,
                                   referentielHabiletesMRSSqlAdapter: ReferentielHabiletesMRSSqlAdapter,
                                   override val config: ImportFileAdapterConfig) extends ImportHabiletesMRS with ImportFileAdapter[HabiletesMRS] {

  override val pattern: String = "habiletes_mrs.csv"

  override def integrerHabiletesMRS: Future[Stream[HabiletesMRS]] = integrerFichiers

  override def integrerFichier(fichier: Path): Future[Stream[HabiletesMRS]] =
    for {
      habiletes <- habiletesMRSCsvAdapter.load(FileIO.fromPath(fichier))
      _ <- referentielHabiletesMRSSqlAdapter.ajouter(habiletes)
    } yield habiletes
}
