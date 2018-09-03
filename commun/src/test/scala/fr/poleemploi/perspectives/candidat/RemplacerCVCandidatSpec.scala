package fr.poleemploi.perspectives.candidat

import java.nio.file.Path
import java.util.UUID

import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, CVService, TypeMedia}
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class RemplacerCVCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatBuilder = new CandidatBuilder
  val cvId: CVId = CVId(UUID.randomUUID().toString)

  val commande: RemplacerCVCommand =
    RemplacerCVCommand(
      id = candidatBuilder.candidatId,
      cvId = cvId,
      nomFichier = "cv.doc",
      typeMedia = TypeMedia.DOC,
      path = mock[Path]
    )

  var cvService: CVService = _

  before {
    cvService = mock[CVService]
  }

  "remplacerCV" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.remplacerCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
      )
    }
    "renvoyer une erreur lorsqu'aucun CV n'existe" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.remplacerCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Impossible de remplacer le CV inexistant du candidat ${candidat.id.value}"
      )
    }
    "renvoyer une erreur lorsque le service externe qui enregistre le CV echoue" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecCV(cvId).build
      when(cvService.update(commande.cvId, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.remplacerCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe "erreur de service"
      )
    }
    "generer un evenement lorsque le CV est remplacé" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecCV(cvId).build
      when(cvService.update(commande.cvId, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.remplacerCV(commande, cvService)

      // Then
      future map (events => events.size mustBe 1)
    }
    "genere un événement contenant les informations modifiees" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecCV(cvId).build
      when(cvService.update(commande.cvId, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.remplacerCV(commande, cvService)

      // Then
      future map (events => {
        val event = events.head.asInstanceOf[CVRemplaceEvent]
        event.candidatId mustBe commande.id
        event.cvId mustBe commande.cvId
      })
    }
  }
}
