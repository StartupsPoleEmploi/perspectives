package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.domain.{CodeROME, RayonRecherche}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Experience}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class ReferentielOffreWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val mapping = new ReferentielOffreWSMapping
  val codeINSEE = "85191"

  var criteresRechercheOffre: CriteresRechercheOffre = _
  var offreResponse: OffreResponse = _

  before {
    criteresRechercheOffre = mock[CriteresRechercheOffre]
    when(criteresRechercheOffre.codesROME) thenReturn List(CodeROME("H2903"))
    when(criteresRechercheOffre.codePostal) thenReturn "85000"
    when(criteresRechercheOffre.rayonRecherche) thenReturn RayonRecherche.MAX_10
    when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT

    offreResponse = mock[OffreResponse]
  }

  "buildRechercherOffresRequest" should {
    "ne doit retourner aucune requete si aucun codeROME n'est renseigné" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn Nil

      // When
      val requests = mapping.buildRechercherOffresRequest(criteresRechercheOffre, codeINSEE)

      // Then
      requests.isEmpty mustBe true
    }
    "doit retourner autant de requetes que de triplés de codesROME (l'API ne gère que 3 codeROME par requete)" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List.tabulate(15)(n => CodeROME(s"H$n"))

      // When
      val requests = mapping.buildRechercherOffresRequest(criteresRechercheOffre, codeINSEE)

      // Then
      requests.size mustBe 5
    }
    "doit valoriser le parametre codeROME" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(CodeROME("A1401"))

      // When
      val requests = mapping.buildRechercherOffresRequest(criteresRechercheOffre, codeINSEE)

      // Then
      requests.size mustBe 1
      requests.head.params.exists(p => p._1 == "codeROME" && p._2 == "A1401") mustBe true
    }
    "doit valoriser le parametre experience" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT

      // When
      val requests = mapping.buildRechercherOffresRequest(criteresRechercheOffre, codeINSEE)

      // Then
      requests.size mustBe 1
      requests.head.params.exists(p => p._1 == "experience" && p._2 == "1") mustBe true
    }
    "doit valoriser le parametre commune" in {
      // Given

      // When
      val requests = mapping.buildRechercherOffresRequest(criteresRechercheOffre, codeINSEE)

      // Then
      requests.size mustBe 1
      requests.head.params.exists(p => p._1 == "commune" && p._2 == codeINSEE) mustBe true
    }
    "doit valoriser le parametre distance" in {
      // Given
      when(criteresRechercheOffre.rayonRecherche) thenReturn RayonRecherche(10)

      // When
      val requests = mapping.buildRechercherOffresRequest(criteresRechercheOffre, codeINSEE)

      // Then
      requests.size mustBe 1
      requests.head.params.exists(p => p._1 == "distance" && p._2 == "10") mustBe true
    }
  }
  "buildOffre" should {
    "ne pas retourner d'offre si l'experience demandée est débutant mais qu'une expérience est exigée (l'API ne prend que le paramètre 'Moins d'un an', il faut filtrer en plus apres)" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT
      when(offreResponse.experienceExige) thenReturn ExperienceExigeResponse.EXIGE

      // When
      val result = mapping.buildOffre(criteresRechercheOffre, offreResponse)

      // Then
      result mustBe None
    }
    "retourner une offre si l'expérience demandée n'est pas débutant" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience("UN_PRO")

      // When
      val result = mapping.buildOffre(criteresRechercheOffre, offreResponse)

      // Then
      result.isDefined mustBe true
    }
  }

}
