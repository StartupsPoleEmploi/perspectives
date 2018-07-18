package fr.poleemploi.perspectives.domain.candidat

import java.util.UUID

import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ModifierNumeroTelephoneSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatId: CandidatId = CandidatId(UUID.randomUUID().toString)

  val commande: ModifierNumeroTelephoneCommand = ModifierNumeroTelephoneCommand(
    id = candidatId,
    numeroTelephone = NumeroTelephone("0234567890")
  )

  val numeroTelephoneModifieEvent =
    NumeroTelephoneModifieEvent(
      numeroTelephone = commande.numeroTelephone.value
    )

  var candidatInscrisEvent: CandidatInscrisEvent = _

  before {
    candidatInscrisEvent = mock[CandidatInscrisEvent]
    when(candidatInscrisEvent.genre) thenReturn Some(Genre.HOMME.code)
  }

  "modifierNumeroTelephone" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = Nil
      )

      // When
      val ex = intercept[RuntimeException] {
        candidat.modifierNumeroTelephone(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} n'est pas encore inscrit"
    }
    "ne pas générer d'événement lorsque le numéro de téléphone ne change pas" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, numeroTelephoneModifieEvent)
      )

      // When
      val result = candidat.modifierNumeroTelephone(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement lorsque le candidat est inscrit et que le numéro de téléphone est saisit la premiere fois" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val result = candidat.modifierNumeroTelephone(commande)

      // Then
      result.size mustBe 1
    }
    "générer un événement lorsque le candidat est inscrit et que le numéro de téléphone change" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent, numeroTelephoneModifieEvent.copy(
          numeroTelephone = "0134767892"
        ))
      )

      // When
      val result = candidat.modifierNumeroTelephone(commande.copy(
        numeroTelephone = NumeroTelephone("0234567890")
      ))

      // Then
      result.size mustBe 1
    }
    "générer un événement contenant le numero de téléphone" in {
      // Given
      val candidat = new Candidat(
        id = candidatId,
        version = 0,
        events = List(candidatInscrisEvent)
      )

      // When
      val result = candidat.modifierNumeroTelephone(commande)

      // Then
      result.head.asInstanceOf[NumeroTelephoneModifieEvent].numeroTelephone mustBe commande.numeroTelephone.value
    }
  }
}
