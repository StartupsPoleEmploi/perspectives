package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, RayonRecherche, UniteLongueur}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Experience, TypeContrat}
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
    when(criteresRechercheOffre.motCle) thenReturn None
    when(criteresRechercheOffre.rayonRecherche) thenReturn None
    when(criteresRechercheOffre.typesContrats) thenReturn Nil
    when(criteresRechercheOffre.secteursActivites) thenReturn Nil
    when(criteresRechercheOffre.codesROME) thenReturn Nil
    when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT

    offreResponse = mock[OffreResponse]
    when(offreResponse.romeCode) thenReturn None
    when(offreResponse.romeLibelle) thenReturn None
    when(offreResponse.competences) thenReturn Nil
    when(offreResponse.qualitesProfessionnelles) thenReturn Nil
    when(offreResponse.permis) thenReturn Nil
    when(offreResponse.formations) thenReturn Nil
    when(offreResponse.langues) thenReturn Nil
    when(offreResponse.logoEntreprise) thenReturn None
    when(offreResponse.experienceExige) thenReturn None
  }

  "buildRechercherOffresRequest" should {
    "doit toujours valoriser le parametre experience" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("experience", "1")) mustBe true
    }
    "doit toujours valoriser le parametre de tri (0 = Tri par pertinence décroissante, distance croissante, date de création décroissante)" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("sort", "0")) mustBe true
    }
    "doit valoriser le parametre motCle lorsqu'il est renseigne" in {
      // Given
      when(criteresRechercheOffre.motCle) thenReturn Some("Soudeur")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "Soudeur")) mustBe true
    }
    "doit valoriser le parametre commune lorsque le codeINSEE est renseigné" in {
      // Given

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = Some(codeINSEE))

      // Then
      request.contains(("commune", codeINSEE)) mustBe true
    }
    "doit valoriser le parametre rayonRecherche lorsqu'il est renseigné avec le codeINSEE" in {
      // Given
      when(criteresRechercheOffre.rayonRecherche) thenReturn Some(RayonRecherche(10, uniteLongueur = UniteLongueur.KM))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = Some(codeINSEE))

      // Then
      request.contains(("distance", "10")) mustBe true
    }
    "ne doit pas valoriser le parametre rayonRecherche lorsque le codeINSEE n'est pas renseigné" in {
      // Given
      when(criteresRechercheOffre.rayonRecherche) thenReturn Some(RayonRecherche(10, uniteLongueur = UniteLongueur.KM))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.exists(p => p._1 == "distance") mustBe false
    }
    "doit valoriser le parametre typeContrat lorsqu'il est renseigné" in {
      // Given
      val typeContrat1 = mock[TypeContrat]
      when(typeContrat1.value) thenReturn "CDD"
      val typeContrat2 = mock[TypeContrat]
      when(typeContrat2.value) thenReturn "CDI"
      when(criteresRechercheOffre.typesContrats) thenReturn List(typeContrat1, typeContrat2)

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("typeContrat", "CDD,CDI")) mustBe true
    }
    "ne pas valoriser le parametre codeROME lorsqu'il n'est pas renseigne" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn Nil

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.exists(p => p._1 == "codeROME") mustBe false
    }
    "doit valoriser le parametre codeROME lorsqu'il est renseigne" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(CodeROME("A1401"), CodeROME("K2204"))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("codeROME", "A1401,K2204")) mustBe true
    }
  }
  "buildOffre" should {
    "ne pas retourner d'offre si l'experience demandée est débutant mais qu'une expérience est exigée (l'API ne prend que le paramètre 'Moins d'un an', il faut filtrer à postériori)" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT
      when(offreResponse.experienceExige) thenReturn Some(ExperienceExigeResponse.EXIGE)

      // When
      val result = mapping.buildOffre(criteresRechercheOffre, offreResponse)

      // Then
      result mustBe None
    }
    "ne pas retourner d'offre si une formation est exigée (l'API ne prend qu'un seul paramètre 'niveauFormation' qui ne permet pas de filtrer suffisamment, il faut filtrer à postériori)" in {
      // Given
      val formationResponse = mock[FormationResponse]
      when(formationResponse.exigence) thenReturn ExigenceResponse.EXIGE
      when(offreResponse.formations) thenReturn List(formationResponse)

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
    "retourner une offre si aucun secteur n'est demandé" in {
      // Given
      when(criteresRechercheOffre.secteursActivites) thenReturn Nil
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.buildOffre(criteresRechercheOffre, offreResponse)

      // Then
      result.isDefined mustBe true
    }
    "ne pas retourner d'offre si le secteur de l'offre n'est pas demandé (l'API ne prend pas ce paramètre en entrée, il faut filtrer à postériori)" in {
      // Given
      val codeSecteurActivite = mock[CodeSecteurActivite]
      when(codeSecteurActivite.value) thenReturn "K"
      when(criteresRechercheOffre.secteursActivites) thenReturn List(codeSecteurActivite)
      when(offreResponse.romeCode) thenReturn Some("H2102")

      // When
      val result = mapping.buildOffre(criteresRechercheOffre, offreResponse)

      // Then
      result mustBe None
    }
    "retourner une offre si le secteur demandé est celui de l'offre" in {
      // Given
      val codeSecteurActivite = mock[CodeSecteurActivite]
      when(codeSecteurActivite.value) thenReturn "K"
      when(criteresRechercheOffre.secteursActivites) thenReturn List(codeSecteurActivite)
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.buildOffre(criteresRechercheOffre, offreResponse)

      // Then
      result.isDefined mustBe true
    }
  }

}
