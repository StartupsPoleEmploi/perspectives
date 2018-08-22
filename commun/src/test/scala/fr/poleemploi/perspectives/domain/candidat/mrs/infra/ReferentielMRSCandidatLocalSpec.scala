package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import java.nio.file.Paths

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

class ReferentielMRSCandidatLocalSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var referentielMRSCandidatLocal: ReferentielMRSCandidatLocal = _
  var referentielMRSCandidatConfig: ReferentielMRSCandidatConfig = _
  var mrsValideeCSVLoader: MRSValideesCSVLoader = _
  var mrsValideesPostgresSql: MRSValideesPostgreSql = _

  before {
    referentielMRSCandidatConfig = mock[ReferentielMRSCandidatConfig]
    mrsValideeCSVLoader = mock[MRSValideesCSVLoader]
    mrsValideesPostgresSql = mock[MRSValideesPostgreSql]

    referentielMRSCandidatLocal = new ReferentielMRSCandidatLocal(
      referentielMRSCandidatConfig = referentielMRSCandidatConfig,
      mrsValideesCSVLoader = mrsValideeCSVLoader,
      mrsValideesPostgresSql = mrsValideesPostgresSql
    )
    when(referentielMRSCandidatConfig.importDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_validees").toURI)
    when(referentielMRSCandidatConfig.archiveDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_validees/archives").toURI)
  }

  "integrerMRSValidees" should {
    "renvoyer une erreur si le répertoire d'import n'existe pas" in {
      // Given
      when(referentielMRSCandidatConfig.importDirectory) thenReturn Paths.get("/home/unknown")

      // When & Then
      recoverToSucceededIf[RuntimeException] {
        referentielMRSCandidatLocal.integrerMRSValidees
      } map {_ =>
        Succeeded
      }
    }
    "renvoyer une erreur si le répertoire d'archive n'existe pas" in {
      // Given
      when(referentielMRSCandidatConfig.archiveDirectory) thenReturn Paths.get("/home/unknown")

      // When & Then
      recoverToSucceededIf[RuntimeException] {
        referentielMRSCandidatLocal.integrerMRSValidees
      } map {_ =>
        Succeeded
      }
    }
    "ne rien faire si aucun fichier n'est présent dans le répertoire d'import" in {
      // Given
      when(referentielMRSCandidatConfig.importDirectory) thenReturn Paths.get("/home/brice/dev/projects/perspectives/commun/src/test/resources")

      // When
      val future = referentielMRSCandidatLocal.integrerMRSValidees

      // Then
      future.map(_ => Succeeded)
    }
  }
}
