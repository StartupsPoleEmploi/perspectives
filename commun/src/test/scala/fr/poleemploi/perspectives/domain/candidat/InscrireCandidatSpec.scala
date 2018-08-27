package fr.poleemploi.perspectives.domain.candidat

import java.time.LocalDate

import fr.poleemploi.perspectives.domain.Genre
import fr.poleemploi.perspectives.domain.candidat.mrs.MRSValidee
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
      val event = result.filter(_.isInstanceOf[CandidatInscrisEvent])
      event.size mustBe 1
      val candidatInscrisEvent = event.head.asInstanceOf[CandidatInscrisEvent]
      candidatInscrisEvent.candidatId mustBe commande.id
      candidatInscrisEvent.nom mustBe commande.nom
      candidatInscrisEvent.prenom mustBe commande.prenom
      candidatInscrisEvent.email mustBe commande.email
      candidatInscrisEvent.genre mustBe commande.genre
    }
    "générer un événement contenant l'adresse" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[AdressePEConnectModifieeEvent])
      event.size mustBe 1
      val adressePEConnectModifieeEvent = event.head.asInstanceOf[AdressePEConnectModifieeEvent]
      adressePEConnectModifieeEvent.candidatId mustBe commande.id
      adressePEConnectModifieeEvent.adresse mustBe commande.adresse
    }
    "générer un événement contenant le statut de demandeur d'emploi" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val result = candidat.inscrire(commande)

      // Then
      val event = result.filter(_.isInstanceOf[StatutDemandeurEmploiPEConnectModifieEvent])
      event.size mustBe 1
      val statutDemandeurEmploiPEConnectModifieEvent = event.head.asInstanceOf[StatutDemandeurEmploiPEConnectModifieEvent]
      statutDemandeurEmploiPEConnectModifieEvent.candidatId mustBe commande.id
      statutDemandeurEmploiPEConnectModifieEvent.statutDemandeurEmploi mustBe commande.statutDemandeurEmploi
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
