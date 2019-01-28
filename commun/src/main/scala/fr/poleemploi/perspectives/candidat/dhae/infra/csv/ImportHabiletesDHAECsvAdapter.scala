package fr.poleemploi.perspectives.candidat.dhae.infra.csv

import java.nio.file.Path

import akka.stream.scaladsl.FileIO
import fr.poleemploi.perspectives.candidat.dhae.domain.{HabiletesDHAE, ImportHabiletesDHAE}
import fr.poleemploi.perspectives.candidat.dhae.infra.sql.ReferentielHabiletesDHAESqlAdapter
import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImportHabiletesDHAECsvAdapter(habiletesDHAECsvAdapter: HabiletesDHAECsvAdapter,
                                    referentielHabiletesDHAESqlAdapter: ReferentielHabiletesDHAESqlAdapter,
                                    override val config: ImportHabiletesDHAECsvAdapterConfig) extends ImportHabiletesDHAE with ImportFileAdapter[HabiletesDHAE] {

  val pattern: String = "habiletes_dhae.csv"

  override def integrerHabiletesDHAE: Future[Stream[HabiletesDHAE]] = integrerFichiers

  override def integrerFichier(fichier: Path): Future[Stream[HabiletesDHAE]] =
    for {
      habiletes <- habiletesDHAECsvAdapter.load(FileIO.fromPath(fichier))
      _ <- referentielHabiletesDHAESqlAdapter.ajouter(habiletes)
    } yield habiletes
}
