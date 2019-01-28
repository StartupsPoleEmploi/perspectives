package fr.poleemploi.perspectives.commun.infra.csv

import java.nio.file.{Path, Paths}

import fr.poleemploi.perspectives.commun.infra.file.{ImportFileAdapter, ImportFileAdapterConfig}
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class ImportFileAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var importFileAdapter: ImportFileAdapter[_] = _
  var config: ImportFileAdapterConfig = _

  before {
    config = mock[ImportFileAdapterConfig]
    when(config.importDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./import_file").toURI)
    when(config.archiveDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./import_file/archives").toURI)

    importFileAdapter = ImportFileTest(config)
  }

  "integrerFichiers" should {
    "renvoyer une erreur si le répertoire d'import n'existe pas" in {
      // Given
      when(config.importDirectory) thenReturn Paths.get("/home/unknown")
      importFileAdapter = ImportFileTest(config)

      // When & Then
      recoverToSucceededIf[IllegalArgumentException] {
        importFileAdapter.integrerFichiers
      } map { _ =>
        Succeeded
      }
    }
    "renvoyer une erreur si le répertoire d'archive n'existe pas" in {
      // Given
      when(config.archiveDirectory) thenReturn Paths.get("/home/unknown")
      importFileAdapter = ImportFileTest(config)

      // When & Then
      recoverToSucceededIf[IllegalArgumentException] {
        importFileAdapter.integrerFichiers
      } map { _ =>
        Succeeded
      }
    }
    "ne rien faire si aucun fichier n'est présent dans le répertoire d'import" in {
      // Given

      // When
      val future = importFileAdapter.integrerFichiers

      // Then
      future.map(_ => Succeeded)
    }
  }
}

case class ImportFileTest(override val config: ImportFileAdapterConfig) extends ImportFileAdapter[Any] {
  override def pattern: String = "*"

  override def integrerFichier(fichier: Path): Future[Stream[Any]] =
    Future.successful(Stream.empty)
}