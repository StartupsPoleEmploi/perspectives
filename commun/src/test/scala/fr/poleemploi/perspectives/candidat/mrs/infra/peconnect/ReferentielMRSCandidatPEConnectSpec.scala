package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Paths

import fr.poleemploi.perspectives.authentification.infra.PEConnectService
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
import fr.poleemploi.perspectives.candidat.mrs.infra.sql.MRSValideesSqlAdapter
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

class ReferentielMRSCandidatPEConnectSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var referentielMRSCandidatPEConnect: ReferentielMRSCandidatPEConnect = _
  var referentielMRSCandidatPEConnectConfig: ReferentielMRSCandidatPEConnectConfig = _
  var mrsValideesCSVAdapter: MRSValideesCSVAdapter = _
  var mrsValideesSqlAdapter: MRSValideesSqlAdapter = _
  var peConnectService: PEConnectService = _

  before {
    referentielMRSCandidatPEConnectConfig = mock[ReferentielMRSCandidatPEConnectConfig]
    mrsValideesCSVAdapter = mock[MRSValideesCSVAdapter]
    mrsValideesSqlAdapter = mock[MRSValideesSqlAdapter]
    peConnectService = mock[PEConnectService]

    when(referentielMRSCandidatPEConnectConfig.importDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_validees").toURI)
    when(referentielMRSCandidatPEConnectConfig.archiveDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./mrs_validees/archives").toURI)

    referentielMRSCandidatPEConnect = new ReferentielMRSCandidatPEConnect(
      config = referentielMRSCandidatPEConnectConfig,
      mrsValideesCSVLoader = mrsValideesCSVAdapter,
      mrsValideesPostgresSql = mrsValideesSqlAdapter,
      peConnectService = peConnectService
    )
  }

  "integrerMRSValidees" should {
    "renvoyer une erreur si le répertoire d'import n'existe pas" in {
      // Given
      when(referentielMRSCandidatPEConnectConfig.importDirectory) thenReturn Paths.get("/home/unknown")
      referentielMRSCandidatPEConnect = new ReferentielMRSCandidatPEConnect(
        config = referentielMRSCandidatPEConnectConfig,
        mrsValideesCSVLoader = mrsValideesCSVAdapter,
        mrsValideesPostgresSql = mrsValideesSqlAdapter,
        peConnectService = peConnectService
      )

      // When & Then
      recoverToSucceededIf[IllegalArgumentException] {
        referentielMRSCandidatPEConnect.integrerMRSValidees
      } map { _ =>
        Succeeded
      }
    }
    "renvoyer une erreur si le répertoire d'archive n'existe pas" in {
      // Given
      when(referentielMRSCandidatPEConnectConfig.archiveDirectory) thenReturn Paths.get("/home/unknown")
      referentielMRSCandidatPEConnect = new ReferentielMRSCandidatPEConnect(
        config = referentielMRSCandidatPEConnectConfig,
        mrsValideesCSVLoader = mrsValideesCSVAdapter,
        mrsValideesPostgresSql = mrsValideesSqlAdapter,
        peConnectService = peConnectService
      )

      // When & Then
      recoverToSucceededIf[IllegalArgumentException] {
        referentielMRSCandidatPEConnect.integrerMRSValidees
      } map { _ =>
        Succeeded
      }
    }
    "ne rien faire si aucun fichier n'est présent dans le répertoire d'import" in {
      // Given

      // When
      val future = referentielMRSCandidatPEConnect.integrerMRSValidees

      // Then
      future.map(_ => Succeeded)
    }
  }
}
