package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class PEConnectWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val mapping = new PEConnectWSMapping

  var resultatRendezVousResponse: ResultatRendezVousResponse = _
  var response: List[ResultatRendezVousResponse] = _

  before {
    resultatRendezVousResponse = mock[ResultatRendezVousResponse]
    when(resultatRendezVousResponse.codeRome) thenReturn "K2204"
    when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn Some("33201")
    when(resultatRendezVousResponse.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE))
    when(resultatRendezVousResponse.dateDebutSession) thenReturn ZonedDateTime.now()

    response = List(resultatRendezVousResponse)
  }

  "buildMRSValidees" should {
    "ne pas retourner de MRS lorsque la reponse ne contient aucun codeResultat" in {
      // Given
      when(resultatRendezVousResponse.listeCodeResultat) thenReturn None

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner de MRS lorsque la reponse ne contient aucun codeSitePESuiviResultat" in {
      // Given
      when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn None

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner de MRS lorsque la reponse contient pas un codeResultat representant une MRS non validée" in {
      // Given
      val codeResultat = CodeResultatRendezVousResponse("NON_VALIDE")
      when(resultatRendezVousResponse.listeCodeResultat) thenReturn Some(List(codeResultat))

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.isEmpty mustBe true
    }
    "retourner une MRSValidee avec le CodeROME" in {
      // Given
      when(resultatRendezVousResponse.codeRome) thenReturn "A1401"

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.exists(_.codeROME == CodeROME("A1401")) mustBe true
    }
    "retourner une MRSValidee avec le CodeDepartement" in {
      // Given
      when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn Some("33201")

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.exists(_.codeDepartement == CodeDepartement("33")) mustBe true
    }
    "retourner une MRSValidee avec la date d'évaluation" in {
      // Given
      val dateMRS = ZonedDateTime.now()
      when(resultatRendezVousResponse.dateDebutSession) thenReturn dateMRS

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.exists(_.dateEvaluation == dateMRS.toLocalDate) mustBe true
    }
    "retourner une MRSValidee non DHAE" in {
      // Given
      when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn Some("33201")

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.exists(_.isDHAE == false) mustBe true
    }
    "retourner les MRSValidee lorsque plusieurs résultats sont retournés avec le même CodeROME et des CodeDepartement différents" in {
      // Given
      val resultat1 = mock[ResultatRendezVousResponse]
      when(resultat1.codeRome) thenReturn "K2204"
      when(resultat1.codeSitePESuiviResultat) thenReturn Some("33201")
      when(resultat1.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE))
      when(resultat1.dateDebutSession) thenReturn ZonedDateTime.now().minusYears(1L)
      val resultat2 = mock[ResultatRendezVousResponse]
      when(resultat2.codeRome) thenReturn "K2204"
      when(resultat2.codeSitePESuiviResultat) thenReturn Some("72201")
      when(resultat2.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE_EMBAUCHE))
      when(resultat2.dateDebutSession) thenReturn ZonedDateTime.now()
      val response = List(resultat1, resultat2)

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.size mustBe 2
    }
    "retourner une seule MRSValidee lorsque plusieurs résultats sont retournés avec le même CodeROME et même CodeDepartement (la saisie a été faite plusieurs fois côté SI PoleEmploi avec un statut différent mais un seul enregistrement nous intéresse : la date importe peu)" in {
      // Given
      val resultat1 = mock[ResultatRendezVousResponse]
      when(resultat1.codeRome) thenReturn "K2204"
      when(resultat1.codeSitePESuiviResultat) thenReturn Some("33201")
      when(resultat1.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE))
      when(resultat1.dateDebutSession) thenReturn ZonedDateTime.now().minusDays(2L)
      val resultat2 = mock[ResultatRendezVousResponse]
      when(resultat2.codeRome) thenReturn "K2204"
      when(resultat2.codeSitePESuiviResultat) thenReturn Some("33201")
      when(resultat2.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE_EMBAUCHE))
      when(resultat2.dateDebutSession) thenReturn ZonedDateTime.now()
      val response = List(resultat1, resultat2)

      // When
      val result = mapping.buildMRSValidees(response)

      // Then
      result.size mustBe 1
    }
  }
}
