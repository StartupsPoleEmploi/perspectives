package fr.poleemploi.perspectives.domain.candidat

import java.nio.file.Path
import java.util.UUID

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.candidat.cv.{CVId, CVService}
import org.mockito.Mockito.when
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class AjouterCVCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)
  val cvId: CVId = CVId(UUID.randomUUID().toString)

  val commande: AjouterCVCommand =
    AjouterCVCommand(
      id = candidatId,
      nomFichier = "cv.doc",
      typeMedia = "application/word",
      path = mock[Path]
    )

  val cvAjouteEvent =
    CVAjouteEvent(
      candidatId = commande.id,
      cvId = cvId
    )

  var candidatInscrisEvent: CandidatInscrisEvent = _
  var cvService: CVService = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME)

    cvService = mock[CVService]
    when(cvService.nextIdentity) thenReturn cvId
  }

  "ajouterCV" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
      )
    }
    "renvoyer une erreur lorsque le CV existe déjà" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, cvAjouteEvent)
      )

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe s"Impossible d'ajouter un CV au candidat ${candidat.id.value}, il existe déjà"
      )
    }
    "renvoyer une erreur lorsque le service externe qui enregistre le CV echoue" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )
      when(cvService.save(cvId, commande.id, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterCV(commande, cvService)
      }.map(ex =>
        ex.getMessage mustBe "erreur de service"
      )
    }
    "generer un evenement lorsque le CV est ajouté" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )
      when(cvService.save(cvId, commande.id, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.ajouterCV(commande, cvService)

      // Then
      future map (events => events.size mustBe 1)
    }
    "genere un événement contenant les informations modifiees" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )
      when(cvService.save(cvId, commande.id, commande.nomFichier, commande.typeMedia, commande.path)) thenReturn Future.successful(())

      // When
      val future = candidat.ajouterCV(commande, cvService)

      // Then
      future map (events => {
        val event = events.head.asInstanceOf[CVAjouteEvent]
        event.candidatId mustBe commande.id
        event.cvId mustBe cvId
      })
    }
  }

}
