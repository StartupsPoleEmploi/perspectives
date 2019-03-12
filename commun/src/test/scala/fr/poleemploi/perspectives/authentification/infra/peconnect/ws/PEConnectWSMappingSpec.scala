package fr.poleemploi.perspectives.authentification.infra.peconnect.ws

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class PEConnectWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val mapping = new PEConnectWSMapping

  var resultatRendezVousResponse: ResultatRendezVousResponse = _

  before {
    resultatRendezVousResponse = mock[ResultatRendezVousResponse]
    when(resultatRendezVousResponse.codeRome) thenReturn "K2204"
    when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn Some("33201")
    when(resultatRendezVousResponse.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE))
    when(resultatRendezVousResponse.dateDebutSession) thenReturn ZonedDateTime.now()
  }

  "buildMRSValidee" should {
    "ne pas retourner de MRS lorsque la reponse ne contient aucun codeResultat" in {
      // Given
      when(resultatRendezVousResponse.listeCodeResultat) thenReturn None

      // When
      val result = mapping.buildMRSValidee(resultatRendezVousResponse)

      // Then
      result mustBe None
    }
    "ne pas retourner de MRS lorsque la reponse ne contient aucun codeSitePESuiviResultat" in {
      // Given
      when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn None

      // When
      val result = mapping.buildMRSValidee(resultatRendezVousResponse)

      // Then
      result mustBe None
    }
    "ne pas retourner de MRS lorsque la reponse contient pas un codeResultat representant une MRS non validée" in {
      // Given
      val codeResultat = CodeResultatRendezVousResponse("NON_VALIDE")
      when(resultatRendezVousResponse.listeCodeResultat) thenReturn Some(List(codeResultat))

      // When
      val result = mapping.buildMRSValidee(resultatRendezVousResponse)

      // Then
      result mustBe None
    }
    "retourner une MRSValidee avec le CodeROME" in {
      // Given
      when(resultatRendezVousResponse.codeRome) thenReturn "A1401"

      // When
      val result = mapping.buildMRSValidee(resultatRendezVousResponse)

      // Then
      result.exists(_.codeROME == CodeROME("A1401")) mustBe true
    }
    "retourner une MRSValidee avec le CodeDepartement" in {
      // Given
      when(resultatRendezVousResponse.codeSitePESuiviResultat) thenReturn Some("33201")

      // When
      val result = mapping.buildMRSValidee(resultatRendezVousResponse)

      // Then
      result.exists(_.codeDepartement == CodeDepartement("33")) mustBe true
    }
    "retourner une MRSValidee avec la date d'évaluation" in {
      // Given
      val dateMRS = ZonedDateTime.now()
      when(resultatRendezVousResponse.dateDebutSession) thenReturn dateMRS

      // When
      val result = mapping.buildMRSValidee(resultatRendezVousResponse)

      // Then
      result.exists(_.dateEvaluation == dateMRS.toLocalDate) mustBe true
    }
  }
}
