package tracking

import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.{CandidatAuthentifie, RecruteurAuthentifie}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.mvc.Flash

class TrackingServiceSpec extends WordSpec
  with MustMatchers with MockitoSugar {

  import TrackingService._

  private val candidatId = "123456789"
  private val recruteurId = "987654321"
  private val email = "no-reply@perspectives.fr"

  "buildTrackingCommun" should {
    "doit renvoyer un tableau vide quand ni candidat ni recruteur n'est connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = None,
        optRecruteurAuthentifie = None
      )

      // Then
      result mustBe Json.arr()
    }
    "doit contenir un champ typeUtilisateur a candidat quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = Some(candidatAuthentifie),
        optRecruteurAuthentifie = None
      )

      // Then
      (result \\ typeUtilisateur).head mustBe JsNumber(typeUtilisateurCandidat)
    }
    "doit contenir un champ candidat_id renseigne quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = Some(candidatAuthentifie),
        optRecruteurAuthentifie = None
      )

      // Then
      (result \\ TrackingService.candidatId).head mustBe JsString(candidatId)
    }
    "ne doit pas contenir un champ email quand candidat connecte sans email renseigne" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, None)

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = Some(candidatAuthentifie),
        optRecruteurAuthentifie = None
      )

      // Then
      (result \\ TrackingService.email).headOption mustBe None
    }
    "doit contenir un champ email renseigne quand candidat connecte et email renseigne" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = Some(candidatAuthentifie),
        optRecruteurAuthentifie = None
      )

      // Then
      (result \\ TrackingService.email).head mustBe JsString(email)
    }
    "doit contenir un champ is_connecte a 1 quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = Some(candidatAuthentifie),
        optRecruteurAuthentifie = None
      )

      // Then
      (result \\ isConnecte).head mustBe JsNumber(1)
    }
    "doit contenir un champ typeUtilisateur a recruteur quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = None,
        optRecruteurAuthentifie = Some(recruteurAuthentifie)
      )

      // Then
      (result \\ typeUtilisateur).head mustBe JsNumber(typeUtilisateurRecruteur)
    }
    "doit contenir un champ recruteur_id renseigne quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = None,
        optRecruteurAuthentifie = Some(recruteurAuthentifie)
      )

      // Then
      (result \\ TrackingService.recruteurId).head mustBe JsString(recruteurId)
    }
    "doit contenir un champ email renseigne quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = None,
        optRecruteurAuthentifie = Some(recruteurAuthentifie)
      )

      // Then
      (result \\ TrackingService.email).head mustBe JsString(email)
    }
    "doit contenir un champ is_connecte a 1 quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(candidatId, email)

      // When
      val result = TrackingService.buildTrackingCommun(
        optCandidatAuthentifie = None,
        optRecruteurAuthentifie = Some(recruteurAuthentifie)
      )

      // Then
      (result \\ isConnecte).head mustBe JsNumber(1)
    }
  }

  "buildTrackingCandidat" should {
    "doit contenir un champ typeUtilisateur a candidat quand candidat pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingCandidat(None)

      // Then
      (result \\ typeUtilisateur).head mustBe JsNumber(typeUtilisateurCandidat)
    }
    "doit contenir un champ typeUtilisateur a candidat quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCandidat(Some(candidatAuthentifie))

      // Then
      (result \\ typeUtilisateur).head mustBe JsNumber(typeUtilisateurCandidat)
    }
    "ne doit pas contenir de champ candidat_id quand candidat pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingCandidat(None)

      // Then
      (result \\ TrackingService.candidatId).headOption mustBe None
    }
    "doit contenir un champ candidat_id quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCandidat(Some(candidatAuthentifie))

      // Then
      (result \\ TrackingService.candidatId).head mustBe JsString(candidatId)
    }
    "ne doit pas contenir de champ email quand candidat pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingCandidat(None)

      // Then
      (result \\ TrackingService.email).headOption mustBe None
    }
    "doit contenir un champ email quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCandidat(Some(candidatAuthentifie))

      // Then
      (result \\ TrackingService.email).head mustBe JsString(email)
    }
    "doit contenir un champ is_connecte a 0 quand candidat pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingCandidat(None)

      // Then
      (result \\ isConnecte).head mustBe JsNumber(0)
    }
    "doit contenir un champ is_connecte a 1 quand candidat connecte" in {
      // Given
      val candidatAuthentifie = mockCandidatAuthentifie(candidatId, Some(email))

      // When
      val result = TrackingService.buildTrackingCandidat(Some(candidatAuthentifie))

      // Then
      (result \\ isConnecte).head mustBe JsNumber(1)
    }
    "ne doit pas contenir de champ event quand pas de flashScope" in {
      // Given & When
      val result = TrackingService.buildTrackingCandidat(None, None)

      // Then
      (result \\ event).headOption mustBe None
    }
    "ne doit pas contenir de champ event quand candidat ne vient pas de se connecter, s'inscrire, se deconnecter ou s'autologuer" in {
      // Given
      val flash = Flash()

      // When
      val result = TrackingService.buildTrackingCandidat(None, Some(flash))

      // Then
      (result \\ event).headOption mustBe None
    }
    "doit contenir un champ event a candidat_connecte quand candidat vient de se connecter" in {
      // Given
      val flash = Flash().withCandidatConnecte

      // When
      val result = TrackingService.buildTrackingCandidat(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventCandidatConnecte)
    }
    "doit contenir un champ event a candidat_inscrit quand candidat vient de s'inscrire" in {
      // Given
      val flash = Flash().withCandidatInscrit

      // When
      val result = TrackingService.buildTrackingCandidat(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventCandidatInscrit)
    }
    "doit contenir un champ event a candidat_autologue quand candidat vient de s'autologuer" in {
      // Given
      val flash = Flash().withCandidatAutologue

      // When
      val result = TrackingService.buildTrackingCandidat(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventCandidatAutologue)
    }
    "doit contenir un champ event a candidat_deconnecte quand candidat vient de se deconnecter" in {
      // Given
      val flash = Flash().withCandidatDeconnecte

      // When
      val result = TrackingService.buildTrackingCandidat(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventCandidatDeconnecte)
    }
  }

  "buildTrackingRecruteur" should {
    "doit contenir un champ typeUtilisateur a recruteur quand recruteur pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingRecruteur(None)

      // Then
      (result \\ typeUtilisateur).head mustBe JsNumber(typeUtilisateurRecruteur)
    }
    "doit contenir un champ typeUtilisateur a recruteur quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ typeUtilisateur).head mustBe JsNumber(typeUtilisateurRecruteur)
    }
    "ne doit pas contenir de champ typeRecruteur quand recruteur connecte sans type recruteur" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ typeRecruteur).headOption mustBe None
    }
    "doit contenir un champ typeRecruteur quand recruteur connecte et recruteur de type entreprise" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email, Some(TypeRecruteur.ENTREPRISE))

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ typeRecruteur).head mustBe JsNumber(typeRecruteurEntreprise)
    }
    "doit contenir un champ typeRecruteur quand recruteur connecte et recruteur de type organisme de formation" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email, Some(TypeRecruteur.ORGANISME_FORMATION))

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ typeRecruteur).head mustBe JsNumber(typeRecruteurOrganismeFormation)
    }
    "doit contenir un champ typeRecruteur quand recruteur connecte et recruteur de type agence interim" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email, Some(TypeRecruteur.AGENCE_INTERIM))

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ typeRecruteur).head mustBe JsNumber(typeRecruteurAgenceInterim)
    }
    "ne doit pas contenir de champ recruteur_id quand recruteur pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingRecruteur(None)

      // Then
      (result \\ TrackingService.recruteurId).headOption mustBe None
    }
    "doit contenir un champ recruteur_id quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ TrackingService.recruteurId).head mustBe JsString(recruteurId)
    }
    "ne doit pas contenir de champ email quand recruteur pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingRecruteur(None)

      // Then
      (result \\ TrackingService.email).headOption mustBe None
    }
    "doit contenir un champ email quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ TrackingService.email).head mustBe JsString(email)
    }
    "ne doit pas contenir de champ is_certifie quand recruteur pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingRecruteur(None)

      // Then
      (result \\ isRecruteurCertifie).headOption mustBe None
    }
    "doit contenir un champ is_certifie a 0 quand recruteur pas certifie" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email, certifie = false)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ isRecruteurCertifie).head mustBe JsNumber(0)
    }
    "doit contenir un champ is_certifie a 1 quand recruteur certifie" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ isRecruteurCertifie).head mustBe JsNumber(1)
    }
    "doit contenir un champ is_connecte a 0 quand recruteur pas connecte" in {
      // Given & When
      val result = TrackingService.buildTrackingRecruteur(None)

      // Then
      (result \\ isConnecte).head mustBe JsNumber(0)
    }
    "doit contenir un champ is_connecte a 1 quand recruteur connecte" in {
      // Given
      val recruteurAuthentifie = mockRecruteurAuthentifie(recruteurId, email)

      // When
      val result = TrackingService.buildTrackingRecruteur(Some(recruteurAuthentifie))

      // Then
      (result \\ isConnecte).head mustBe JsNumber(1)
    }
    "ne doit pas contenir de champ event quand pas de flashScope" in {
      // Given & When
      val result = TrackingService.buildTrackingRecruteur(None, None)

      // Then
      (result \\ event).headOption mustBe None
    }
    "ne doit pas contenir de champ event quand recruteur ne vient pas de se connecter, s'inscrire ou se deconnecter" in {
      // Given
      val flash = Flash()

      // When
      val result = TrackingService.buildTrackingRecruteur(None, Some(flash))

      // Then
      (result \\ event).headOption mustBe None
    }
    "doit contenir un champ event a recruteur_connecte quand recruteur vient de se connecter" in {
      // Given
      val flash = Flash().withRecruteurConnecte

      // When
      val result = TrackingService.buildTrackingRecruteur(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventRecruteurConnecte)
    }
    "doit contenir un champ event a recruteur_inscrit quand recruteur vient de s'inscrire" in {
      // Given
      val flash = Flash().withRecruteurInscrit

      // When
      val result = TrackingService.buildTrackingRecruteur(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventRecruteurInscrit)
    }
    "doit contenir un champ event a recruteur_deconnecte quand recruteur vient de se deconnecter" in {
      // Given
      val flash = Flash().withRecruteurDeconnecte

      // When
      val result = TrackingService.buildTrackingRecruteur(None, Some(flash))

      // Then
      (result \\ event).head mustBe JsString(eventRecruteurDeconnecte)
    }
  }

  private def mockCandidatAuthentifie(candidatId: String,
                                      email: Option[String]): CandidatAuthentifie = {
    val candidatAuthentifie = mock[CandidatAuthentifie]
    when(candidatAuthentifie.candidatId).thenReturn(CandidatId(candidatId))
    when(candidatAuthentifie.email).thenReturn(email.map(Email(_)))
    candidatAuthentifie
  }

  private def mockRecruteurAuthentifie(recruteurId: String,
                                       email: String,
                                       typeRecruteur: Option[TypeRecruteur] = None,
                                       certifie: Boolean = true): RecruteurAuthentifie = {
    val recruteurAuthentifie = mock[RecruteurAuthentifie]
    when(recruteurAuthentifie.recruteurId).thenReturn(RecruteurId(recruteurId))
    when(recruteurAuthentifie.email).thenReturn(Email(email))
    when(recruteurAuthentifie.typeRecruteur).thenReturn(typeRecruteur)
    when(recruteurAuthentifie.certifie).thenReturn(certifie)
    recruteurAuthentifie
  }
}
