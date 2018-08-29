package fr.poleemploi.perspectives.candidat.mrs.infra.file

import java.nio.file.Paths

import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

class ReferentielMRSCandidatFileSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var referentielMRSCandidatFile: ReferentielMRSCandidatFile = _
  var referentielMRSCandidatFileConfig: ReferentielMRSCandidatFileConfig = _
  var mrsValideesCSVAdapter: MRSValideesCSVAdapter = _
  var mrsValideesSqlAdapter: MRSValideesSqlAdapter = _

  before {
    referentielMRSCandidatFileConfig = mock[ReferentielMRSCandidatFileConfig]
    mrsValideesCSVAdapter = mock[MRSValideesCSVAdapter]
    mrsValideesSqlAdapter = mock[MRSValideesSqlAdapter]

    when(referentielMRSCandidatFileConfig.importDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_validees").toURI)
    when(referentielMRSCandidatFileConfig.archiveDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_validees/archives").toURI)

    referentielMRSCandidatFile = new ReferentielMRSCandidatFile(
      referentielMRSCandidatFileConfig = referentielMRSCandidatFileConfig,
      mrsValideesCSVLoader = mrsValideesCSVAdapter,
      mrsValideesPostgresSql = mrsValideesSqlAdapter
    )
  }

  "integrerMRSValidees" should {
    "renvoyer une erreur si le répertoire d'import n'existe pas" in {
      // Given
      when(referentielMRSCandidatFileConfig.importDirectory) thenReturn Paths.get("/home/unknown")
      referentielMRSCandidatFile = new ReferentielMRSCandidatFile(
        referentielMRSCandidatFileConfig = referentielMRSCandidatFileConfig,
        mrsValideesCSVLoader = mrsValideesCSVAdapter,
        mrsValideesPostgresSql = mrsValideesSqlAdapter
      )

      // When & Then
      recoverToSucceededIf[RuntimeException] {
        referentielMRSCandidatFile.integrerMRSValidees
      } map {_ =>
        Succeeded
      }
    }
    "renvoyer une erreur si le répertoire d'archive n'existe pas" in {
      // Given
      when(referentielMRSCandidatFileConfig.archiveDirectory) thenReturn Paths.get("/home/unknown")
      referentielMRSCandidatFile = new ReferentielMRSCandidatFile(
        referentielMRSCandidatFileConfig = referentielMRSCandidatFileConfig,
        mrsValideesCSVLoader = mrsValideesCSVAdapter,
        mrsValideesPostgresSql = mrsValideesSqlAdapter
      )

      // When & Then
      recoverToSucceededIf[RuntimeException] {
        referentielMRSCandidatFile.integrerMRSValidees
      } map {_ =>
        Succeeded
      }
    }
    "ne rien faire si aucun fichier n'est présent dans le répertoire d'import" in {
      // Given

      // When
      val future = referentielMRSCandidatFile.integrerMRSValidees

      // Then
      future.map(_ => Succeeded)
    }
  }
}
