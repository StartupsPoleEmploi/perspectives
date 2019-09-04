package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class AutologgerCandidatSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val commande: AutologgerCandidatCommand =
    AutologgerCandidatCommand(
      id = candidatBuilder.candidatId
    )

  "autologger" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      val ex = intercept[IllegalStateException](
        candidat.autologger(commande)
      )

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "générer un événement de connexion lorsqu on autologge le candidat" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom =  Some(Nom("Patulacci")),
          prenom = Some(Prenom("Marcel")),
          email = Some(Email("marcel.patulacci@email.com")),
          genre = Some(Genre.HOMME)
        )
        .build

      // When
      val events = candidat.autologger(commande)

      // Then
      events.count(_.isInstanceOf[CandidatAutologgeEvent]) mustBe 1
      val event = events.head.asInstanceOf[CandidatAutologgeEvent]
      event.candidatId mustBe commande.id
    }
  }
}
