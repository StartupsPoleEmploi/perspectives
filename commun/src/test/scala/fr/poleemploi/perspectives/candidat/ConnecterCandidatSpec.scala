package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain.{Email, Genre}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ConnecterCandidatSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatBuilder = new CandidatBuilder

  val adresse: Adresse =
    Adresse(
      voie = "3 rue des oursons",
      codePostal = "75020",
      libelleCommune = "Paris",
      libellePays = "France"
    )
  val statutDemandeurEmploi: StatutDemandeurEmploi = StatutDemandeurEmploi.DEMANDEUR_EMPLOI

  val commande: ConnecterCandidatCommand =
    ConnecterCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = "nouveau nom",
      prenom = "nouveau prenom",
      email = Email("nouveau email"),
      genre = Genre.HOMME,
      adresse = None,
      statutDemandeurEmploi = None
    )

  "modifierProfil" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[IllegalArgumentException] {
        candidat.connecter(commande)
      }

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "générer un événement de connexion lorsqu'aucune information de profil n'est modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(
          nom = Some(commande.nom),
          prenom = Some(commande.prenom),
          email = Some(commande.email),
          genre = Some(commande.genre)
        )
        .build

      // When
      val result = candidat.connecter(commande)

      // Then
      result.count(_.isInstanceOf[CandidatConnecteEvent]) mustBe 1
    }
    "générer un événement si une information de profil a été saisie pour la premiere fois" in {
      // Given
      val candidat = candidatBuilder.avecInscription()
        .build

      // When
      val result = candidat.connecter(commande)

      // Then
      result.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si le nom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(nom = Some("ancien nom"))
        .build

      // When
      val result = candidat.connecter(commande.copy(
        nom = "nouveau nom"
      ))

      // Then
      result.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si le prénom a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(prenom = Some("ancien prénom"))
        .build

      // When
      val result = candidat.connecter(commande.copy(
        prenom = "nouveau prénom"
      ))

      // Then
      result.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si l'email a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(email = Some(Email("ancien-email@domain.fr")))
        .build

      // When
      val result = candidat.connecter(commande.copy(
        email = Email("nouvel-email@domain.fr")
      ))

      // Then
      result.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement si le genre a été modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription(genre = Some(Genre.HOMME))
        .build

      // When
      val result = candidat.connecter(commande.copy(
        genre = Genre.FEMME
      ))

      // Then
      result.count(_.isInstanceOf[ProfilCandidatModifieEvent]) mustBe 1
    }
    "générer un événement contenant les informations de profil modifiées" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val result = candidat.connecter(commande)

      // Then
      val event = result.filter(_.isInstanceOf[ProfilCandidatModifieEvent]).head.asInstanceOf[ProfilCandidatModifieEvent]
      event.candidatId mustBe commande.id
      event.nom mustBe commande.nom
      event.prenom mustBe commande.prenom
      event.email mustBe commande.email
      event.genre mustBe commande.genre
    }
    "générer un événement si l'adresse a été modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val result = candidat.connecter(commande.copy(
        adresse = Some(adresse.copy(voie = "nouvelle voie"))
      ))

      // Then
      result.count(_.isInstanceOf[AdresseModifieeEvent]) mustBe 1
    }
    "générer un événement contenant l'adresse modifiée" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecAdresse(adresse.copy(voie = "ancienne voie"))
        .build

      // When
      val result = candidat.connecter(commande.copy(
        adresse = Some(adresse.copy(voie = "nouvelle voie"))
      ))

      // Then
      val event = result.filter(_.isInstanceOf[AdresseModifieeEvent]).head.asInstanceOf[AdresseModifieeEvent]
      event.candidatId mustBe commande.id
      event.adresse.voie mustBe "nouvelle voie"
    }
    "générer un événement contenant le statut de demandeur d'emploi modifié" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .avecStatutDemandeurEmploi(StatutDemandeurEmploi.DEMANDEUR_EMPLOI)
        .build

      // When
      val result = candidat.connecter(commande.copy(
        statutDemandeurEmploi = Some(StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI)
      ))

      // Then
      val event = result.filter(_.isInstanceOf[StatutDemandeurEmploiModifieEvent]).head.asInstanceOf[StatutDemandeurEmploiModifieEvent]
      event.candidatId mustBe commande.id
      event.statutDemandeurEmploi mustBe StatutDemandeurEmploi.NON_DEMANDEUR_EMPLOI
    }
  }
}
