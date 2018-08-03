package fr.poleemploi.perspectives.domain.candidat

import java.nio.file.Path
import java.util.UUID

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.candidat.cv.{CVId, CVService}
import org.mockito.Mockito.when
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class RemplacerCVCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)
  val cvId: CVId = CVId(UUID.randomUUID().toString)

  val commande: RemplacerCVCommand =
    RemplacerCVCommand(
      id = candidatId,
      cvId = cvId,
      nomFichier = "cv.doc",
      typeMedia = "application/word",
      path = mock[Path]
    )

  val cvAjouteEvent =
    CVAjouteEvent(
      candidatId = commande.id,
      cvId = commande.cvId
    )

  val cvRemplaceEvent =
    CVRemplaceEvent(
      candidatId = commande.id,
      cvId = commande.cvId
    )

  var candidatInscrisEvent: CandidatInscrisEvent = _
  var cvService: CVService = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME)

    cvService = mock[CVService]
  }

  "remplacerCV" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.remplacerCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
      )
    }
    "renvoyer une erreur lorsqu'aucun CV n'existe" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.remplacerCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Impossible de remplacer le CV inexistant du candidat ${candidat.id.value}"
      )
    }
    "renvoyer une erreur lorsque le service externe qui enregistre le CV echoue" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, cvAjouteEvent)
      )
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
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, cvAjouteEvent)
      )
      when(cvService.update(commande.cvId, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.remplacerCV(commande, cvService)

      // Then
      future map (events => events.size mustBe 1)
    }
    "genere un événement contenant les informations modifiees" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, cvAjouteEvent)
      )
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
