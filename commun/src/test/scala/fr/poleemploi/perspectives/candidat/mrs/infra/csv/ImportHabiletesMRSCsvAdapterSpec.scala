package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.nio.file.Paths

import fr.poleemploi.perspectives.candidat.mrs.infra.sql.ReferentielHabiletesMRSSqlAdapter
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

class ImportHabiletesMRSCsvAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var importHabiletesMRSCsvAdapter: ImportHabiletesMRSCsvAdapter = _
  var config: ImportHabiletesMRSCsvAdapterConfig = _
  var habiletesMRSCsvAdapter: HabiletesMRSCsvAdapter = _
  var referentielHabiletesMRSSqlAdapter: ReferentielHabiletesMRSSqlAdapter = _

  before {
    config = mock[ImportHabiletesMRSCsvAdapterConfig]
    habiletesMRSCsvAdapter = mock[HabiletesMRSCsvAdapter]
    referentielHabiletesMRSSqlAdapter = mock[ReferentielHabiletesMRSSqlAdapter]

    when(config.importDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_habiletes").toURI)
    when(config.archiveDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_habiletes/archives").toURI)

    importHabiletesMRSCsvAdapter = new ImportHabiletesMRSCsvAdapter(
      config = config,
      habiletesMRSCsvAdapter = habiletesMRSCsvAdapter,
      referentielHabiletesMRSSqlAdapter = referentielHabiletesMRSSqlAdapter
    )
  }

  "integrerHabiletesMRS" should {
    "renvoyer une erreur si le répertoire d'import n'existe pas" in {
      // Given
      when(config.importDirectory) thenReturn Paths.get("/home/unknown")
      importHabiletesMRSCsvAdapter = new ImportHabiletesMRSCsvAdapter(
        config = config,
        habiletesMRSCsvAdapter = habiletesMRSCsvAdapter,
        referentielHabiletesMRSSqlAdapter = referentielHabiletesMRSSqlAdapter
      )

      // When & Then
      recoverToSucceededIf[IllegalArgumentException] {
        importHabiletesMRSCsvAdapter.integrerHabiletesMRS
      } map { _ =>
        Succeeded
      }
    }
    "renvoyer une erreur si le répertoire d'archive n'existe pas" in {
      // Given
      when(config.archiveDirectory) thenReturn Paths.get("/home/unknown")
      importHabiletesMRSCsvAdapter = new ImportHabiletesMRSCsvAdapter(
        config = config,
        habiletesMRSCsvAdapter = habiletesMRSCsvAdapter,
        referentielHabiletesMRSSqlAdapter = referentielHabiletesMRSSqlAdapter
      )

      // When & Then
      recoverToSucceededIf[IllegalArgumentException] {
        importHabiletesMRSCsvAdapter.integrerHabiletesMRS
      } map { _ =>
        Succeeded
      }
    }
    "ne rien faire si aucun fichier n'est présent dans le répertoire d'import" in {
      // Given

      // When
      val future = importHabiletesMRSCsvAdapter.integrerHabiletesMRS

      // Then
      future.map(_ => Succeeded)
    }
  }

}
