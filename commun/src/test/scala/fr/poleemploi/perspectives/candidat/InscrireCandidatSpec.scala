package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.Genre
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}

class InscrireCandidatSpec extends WordSpec with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val mrsValidee = MRSValidee(
    codeMetier = "H083",
    dateEvaluation = LocalDate.now()
  )

  val commande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = "nom",
      prenom = "prenom",
      email = "email@domain.com",
      genre = Genre.HOMME,
      adresse = mock[Adresse],
      statutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI,
      mrsValidees = Nil
    )

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val ex = intercept[RuntimeException] {
        candidat.inscrire(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} est déjà inscrit"
    }
    "générer des événements lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      result.size mustBe 3
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[CandidatInscritEvent])
      event.size mustBe 1
      val candidatInscritEvent = event.head.asInstanceOf[CandidatInscritEvent]
      candidatInscritEvent.candidatId mustBe commande.id
      candidatInscritEvent.nom mustBe commande.nom
      candidatInscritEvent.prenom mustBe commande.prenom
      candidatInscritEvent.email mustBe commande.email
      candidatInscritEvent.genre mustBe commande.genre
    }
    "générer un événement contenant l'adresse" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[AdresseModifieeEvent])
      event.size mustBe 1
      val adresseModifieeEvent = event.head.asInstanceOf[AdresseModifieeEvent]
      adresseModifieeEvent.candidatId mustBe commande.id
      adresseModifieeEvent.adresse mustBe commande.adresse
    }
    "générer un événement contenant le statut de demandeur d'emploi" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent])
      event.size mustBe 1
      val statutDemandeurEmploiModifieEvent = event.head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
      statutDemandeurEmploiModifieEvent.candidatId mustBe commande.id
      statutDemandeurEmploiModifieEvent.statutDemandeurEmploi mustBe commande.statutDemandeurEmploi
    }
    "générer un événement contenant la liste des MRS validées" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande.copy(
        mrsValidees = List(mrsValidee)
      ))

      // Then
      val event = result.filter(_.isInstanceOf[MRSAjouteeEvent])
      event.size mustBe 1
      val mrsAjouteeEvent = event.head.asInstanceOf[MRSAjouteeEvent]
      mrsAjouteeEvent.candidatId mustBe commande.id
      mrsAjouteeEvent.metier mustBe mrsValidee.codeMetier
      mrsAjouteeEvent.dateEvaluation mustBe mrsValidee.dateEvaluation
    }
  }

}
