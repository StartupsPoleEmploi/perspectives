package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Paths

import akka.actor.ActorSystem
import fr.poleemploi.perspectives.authentification.infra.peconnect.sql.PEConnectSqlAdapter
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

class ImportMRSCandidatPEConnectSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  var referentielMRSCandidatPEConnect: ImportMRSCandidatPEConnect = _
  var config: ImportMRSCandidatPEConnectConfig = _
  var mrsValideesCandidatsCSVAdapter: MRSValideesCandidatsCSVAdapter = _
  var mrsValideesCandidatsSqlAdapter: MRSValideesCandidatsSqlAdapter = _
  var peConnectSqlAdapter: PEConnectSqlAdapter = _

  before {
    config = mock[ImportMRSCandidatPEConnectConfig]
    mrsValideesCandidatsCSVAdapter = mock[MRSValideesCandidatsCSVAdapter]
    mrsValideesCandidatsSqlAdapter = mock[MRSValideesCandidatsSqlAdapter]
    peConnectSqlAdapter = mock[PEConnectSqlAdapter]

    when(config.importDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./candidats_mrs_validees").toURI)
    when(config.archiveDirectory) thenReturn Paths.get(getClass.getClassLoader.getResource("./candidats_mrs_validees/archives").toURI)

    referentielMRSCandidatPEConnect = new ImportMRSCandidatPEConnect(
      config = config,
      actorSystem = actorSystem,
      mrsValideesCandidatsCSVAdapter = mrsValideesCandidatsCSVAdapter,
      mrsValideesCandidatsSqlAdapter = mrsValideesCandidatsSqlAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter
    )
  }

  "integrerMRSValidees" should {
    "renvoyer une erreur si le répertoire d'import n'existe pas" in {
      // Given
      when(config.importDirectory) thenReturn Paths.get("/home/unknown")
      referentielMRSCandidatPEConnect = new ImportMRSCandidatPEConnect(
        config = config,
        actorSystem = actorSystem,
        mrsValideesCandidatsCSVAdapter = mrsValideesCandidatsCSVAdapter,
        mrsValideesCandidatsSqlAdapter = mrsValideesCandidatsSqlAdapter,
        peConnectSqlAdapter = peConnectSqlAdapter
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
      when(config.archiveDirectory) thenReturn Paths.get("/home/unknown")
      referentielMRSCandidatPEConnect = new ImportMRSCandidatPEConnect(
        config = config,
        actorSystem = actorSystem,
        mrsValideesCandidatsCSVAdapter = mrsValideesCandidatsCSVAdapter,
        mrsValideesCandidatsSqlAdapter = mrsValideesCandidatsSqlAdapter,
        peConnectSqlAdapter = peConnectSqlAdapter
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
