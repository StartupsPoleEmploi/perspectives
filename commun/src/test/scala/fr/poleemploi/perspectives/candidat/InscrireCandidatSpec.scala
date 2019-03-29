package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain.{Coordonnees, Email, Genre}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

import scala.concurrent.Future

class InscrireCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatBuilder = new CandidatBuilder

  val commande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = "nom",
      prenom = "prenom",
      email = Email("email@domain.com"),
      genre = Genre.HOMME,
      adresse = None,
      statutDemandeurEmploi = None
    )

  var localisationService: LocalisationService = _

  before {
    localisationService = mock[LocalisationService]
  }

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalStateException](
        candidat.inscrire(commande, localisationService)
      ).map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} dans l'état Inscrit ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "générer un evenement lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande, localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[CandidatInscritEvent]) mustBe 1)
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande, localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[CandidatInscritEvent]).head.asInstanceOf[CandidatInscritEvent]
        event.candidatId mustBe commande.id
        event.nom mustBe commande.nom
        event.prenom mustBe commande.prenom
        event.email mustBe commande.email
        event.genre mustBe commande.genre
      })
    }
    "générer un événement contenant l'adresse lorsqu'elle est renseignée" in {
      // Given
      val adresse = mock[Adresse]
      val coordonnees = mock[Coordonnees]
      val candidat = candidatBuilder.build
      when(localisationService.localiser(adresse)) thenReturn Future.successful(Some(coordonnees))

      // When
      val future = candidat.inscrire(commande.copy(
        adresse = Some(adresse)
      ), localisationService)

      // Then
      future map (events => {
        val adresseModifieeEvent = events.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
        adresseModifieeEvent.candidatId mustBe commande.id
        adresseModifieeEvent.adresse mustBe adresse
        adresseModifieeEvent.coordonnees mustBe Some(coordonnees)
      })
    }
    "générer un événement contenant l'adresse lorsqu'elle est renseignée même si le service de localisation échoue" in {
      // Given
      val adresse = mock[Adresse]
      val candidat = candidatBuilder.build
      when(localisationService.localiser(adresse)) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When
      val future = candidat.inscrire(commande.copy(
        adresse = Some(adresse)
      ), localisationService)

      // Then
      future map (events => {
        val adresseModifieeEvent = events.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
        adresseModifieeEvent.candidatId mustBe commande.id
        adresseModifieeEvent.adresse mustBe adresse
        adresseModifieeEvent.coordonnees mustBe None
      })
    }
    "générer un événement contenant le statut de demandeur d'emploi lorsqu'il est renseigné" in {
      // Given
      val statutDemandeurEmploi = mock[StatutDemandeurEmploi]
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande.copy(
        statutDemandeurEmploi = Some(statutDemandeurEmploi)
      ), localisationService)

      // Then
      future map (events => {
        val statutDemandeurEmploiModifieEvent = events.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]).head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
        statutDemandeurEmploiModifieEvent.candidatId mustBe commande.id
        statutDemandeurEmploiModifieEvent.statutDemandeurEmploi mustBe statutDemandeurEmploi
      })
    }
  }

}
