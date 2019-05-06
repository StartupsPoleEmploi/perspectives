package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.candidat.localisation.domain.LocalisationService
import fr.poleemploi.perspectives.commun.domain._
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
      nom = Nom("nom"),
      prenom = Prenom("prenom"),
      email = Email("email@domain.com"),
      genre = Genre.HOMME,
      adresse = None,
      statutDemandeurEmploi = None
    )

  var localisationService: LocalisationService = _
  var adresse: Adresse = _

  before {
    localisationService = mock[LocalisationService]
    adresse = mock[Adresse]
  }

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalStateException](
        candidat.inscrire(commande, localisationService)
      ).map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut INSCRIT ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "ne pas générer d'événement de modification d'adresse lorsque le service de localisation échoue (pas d'intérêt pour la recherche)" in {
      // Given
      when(localisationService.localiser(adresse)) thenReturn Future.failed(new RuntimeException("erreur de service"))
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande.copy(
        adresse = Some(adresse)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 0)
    }
    "ne pas générer d'événement de modification d'adresse lorsque l'adresse n'a pas de coordonnées (pas d'intérêt pour la recherche)" in {
      // Given
      when(localisationService.localiser(adresse)) thenReturn Future.successful(None)
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande.copy(
        adresse = Some(adresse)
      ), localisationService)

      // Then
      future map (events => events.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 0)
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
      val coordonnees = mock[Coordonnees]
      val candidat = candidatBuilder.build
      when(localisationService.localiser(adresse)) thenReturn Future.successful(Some(coordonnees))

      // When
      val future = candidat.inscrire(commande.copy(
        adresse = Some(adresse)
      ), localisationService)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
        event.candidatId mustBe commande.id
        event.adresse mustBe adresse
        event.coordonnees mustBe coordonnees
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
        val event = events.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]).head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
        event.candidatId mustBe commande.id
        event.statutDemandeurEmploi mustBe statutDemandeurEmploi
      })
    }
  }

}
