package fr.poleemploi.perspectives.offre.infra.ws

import fr.poleemploi.perspectives.commun.domain.{CodeDomaineProfessionnel, CodeROME, CodeSecteurActivite, UniteLongueur}
import fr.poleemploi.perspectives.offre.domain._
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
    when(criteresRechercheOffre.motsCles) thenReturn Nil
    when(criteresRechercheOffre.rayonRecherche) thenReturn None
    when(criteresRechercheOffre.typesContrats) thenReturn Nil
    when(criteresRechercheOffre.secteursActivites) thenReturn Nil
    when(criteresRechercheOffre.codesROME) thenReturn Nil
    when(criteresRechercheOffre.codesDomaineProfessionnels) thenReturn Nil
    when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT
    when(criteresRechercheOffre.page) thenReturn None

    offreResponse = mock[OffreResponse]
    when(offreResponse.romeCode) thenReturn Some("A1401")
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
    "doit valoriser le parametre experience" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("experience", "1")) mustBe true
    }
    "doit renvoyer une erreur si le parametre experience demandée n'est pas gérée" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience("NON_GEREE")

      // When
      val ex = intercept[IllegalArgumentException](
        mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)
      )

      // Then
      ex.getMessage mustBe "Expérience non gérée : NON_GEREE"
    }
    "doit toujours valoriser le parametre de tri (0 = Tri par pertinence décroissante, distance croissante, date de création décroissante)" in {
      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("sort", "0")) mustBe true
    }
    "doit toujours valoriser le parametre origineOffre pour ne prendre que les offres de PoleEmploi et exclure celle des partenaires" in {
      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("origineOffre", "1")) mustBe true
    }
    "doit valoriser le parametre motsCles lorsqu'un motCle est renseigne" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("Soudeur")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "Soudeur")) mustBe true
    }
    "ne doit pas valoriser les motsCles s'ils font moins de deux caractères" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("à")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.exists(_._1 == "motsCles") mustBe false
    }
    "doit exclure les motsCles qui font moins de deux caractères" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("soudeur", "à", "industrie")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "soudeur,industrie")) mustBe true
    }
    "doit remplacer les virgules contenues dans les motsCles par des espaces pour prendre en compte tout le motCle et ne pas renvoyer d'erreur (la virgule est le caractère de séparation entre les motsCles dans l'API)" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("soudeur,industrie", "interim")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "soudeur industrie,interim")) mustBe true
    }
    "doit conserver les apostrophes dans les motsCles" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("d'industrie")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "d'industrie")) mustBe true
    }
    "doit supprimer les caractères spéciaux autres que les virgules et les apostrophes dans les motsCles" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("@#soudeur$%^/-\"\",industrie&+.")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "soudeur industrie")) mustBe true
    }
    "doit valoriser les motCles en les séparant par des virgules lorsqu'on a plusieurs mots clés" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List("soudeur", "industrie")

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "soudeur,industrie")) mustBe true
    }
    "doit valoriser les motCles en prennant en compte 7 mots maximum (limite de l'API)" in {
      // Given
      when(criteresRechercheOffre.motsCles) thenReturn List(
        "soudeur", "industrie", "decoupe",
        "conduite", "soudage", "robot",
        "interim", "longue", "recherche"
      )

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("motsCles", "soudeur,industrie,decoupe,conduite,soudage,robot,interim")) mustBe true
    }
    "doit valoriser le parametre commune lorsque le codeINSEE est renseigné" in {
      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = Some(codeINSEE))

      // Then
      request.contains(("commune", codeINSEE)) mustBe true
    }
    "doit renvoyer une erreur si le parametre rayonRecherche demandé n'est pas géré" in {
      // Given
      when(criteresRechercheOffre.rayonRecherche) thenReturn Some(RayonRecherche(value = 10, uniteLongueur = UniteLongueur("CM")))

      // When
      val ex = intercept[IllegalArgumentException](
        mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = Some(codeINSEE))
      )

      // Then
      ex.getMessage must startWith("Rayon de recherche non géré")
    }
    "ne doit pas valoriser le parametre rayonRecherche lorsque le codeINSEE n'est pas renseigné" in {
      // Given
      when(criteresRechercheOffre.rayonRecherche) thenReturn Some(RayonRecherche(10, uniteLongueur = UniteLongueur.KM))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("distance", "10")) mustBe false
    }
    "doit valoriser le parametre rayonRecherche lorsqu'il est renseigné avec le codeINSEE" in {
      // Given
      when(criteresRechercheOffre.rayonRecherche) thenReturn Some(RayonRecherche(10, uniteLongueur = UniteLongueur.KM))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = Some(codeINSEE))

      // Then
      request.contains(("distance", "10")) mustBe true
    }
    "doit valoriser le parametre typeContrat lorsqu'il est renseigné" in {
      // Given
      when(criteresRechercheOffre.typesContrats) thenReturn List(TypeContrat("CDD"), TypeContrat("CDI"))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("typeContrat", "CDD,CDI")) mustBe true
    }
    "ne pas valoriser le parametre codeROME lorsqu'ils ne sont pas renseignés" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn Nil

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.exists(p => p._1 == "codeROME") mustBe false
    }
    "ne pas valoriser le parametre codeROME lorsqu'ils sont renseignés mais qu'on en a plus que 3 (nombre max géré pour un appel par l'API)" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List.tabulate(4)(n => CodeROME(s"A140$n"))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.exists(p => p._1 == "codeROME") mustBe false
    }
    "doit valoriser le parametre codeROME lorsqu'ils sont renseignes et qu'ils ne dépassent pas 3 valeurs (nombre max géré pour un appel par l'API)" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(CodeROME("A1401"), CodeROME("K2204"))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("codeROME", "A1401,K2204")) mustBe true
    }
    "ne doit pas valoriser le parametre range lorsqu'aucuune page spécifique n'est demandée" in {
      // Given
      when(criteresRechercheOffre.page) thenReturn None

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.exists(p => p._1 == "range") mustBe false
    }
    "doit valoriser le parametre range lorsqu'une page spécifique est demandée" in {
      // Given
      when(criteresRechercheOffre.page) thenReturn Some(PageOffres(debut = 0, fin = 149))

      // When
      val request = mapping.buildRechercherOffresRequest(criteresRechercheOffre = criteresRechercheOffre, codeINSEE = None)

      // Then
      request.contains(("range", "0-149")) mustBe true
    }
  }
  "filterOffres" should {
    "ne pas retourner l'offre lorsqu'elle ne comporte pas de codeROME" in {
      // Given
      when(offreResponse.romeCode) thenReturn None

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner l'offre lorsque l'experience demandée est débutant mais qu'une expérience est exigée (l'API ne prend que le paramètre 'Moins d'un an', il faut filtrer à postériori)" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT
      when(offreResponse.experienceExige) thenReturn Some(ExperienceExigeResponse.EXIGE)

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'offre lorsqu'elle correspond à l'expérience demandée" in {
      // Given
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT
      when(offreResponse.experienceExige) thenReturn Some(ExperienceExigeResponse.DEBUTANT_ACCEPTE)

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "ne pas retourner l'offre lorsqu'elle contient une formation exigée et que l'experience demandée est débutant (l'API ne prend qu'un seul paramètre 'niveauFormation' qui ne permet pas de filtrer suffisamment, il faut filtrer à postériori)" in {
      // Given
      val formationResponse = mock[FormationResponse]
      when(formationResponse.exigence) thenReturn ExigenceResponse.EXIGE
      when(criteresRechercheOffre.experience) thenReturn Experience.DEBUTANT
      when(offreResponse.formations) thenReturn List(formationResponse)

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'offre lorsqu'elle contient une formation exigée mais que l'experience demandée n'est pas débutant" in {
      // Given
      val formationResponse = mock[FormationResponse]
      when(formationResponse.exigence) thenReturn ExigenceResponse.EXIGE
      when(criteresRechercheOffre.experience) thenReturn Experience("PRO")
      when(offreResponse.formations) thenReturn List(formationResponse)

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "ne pas retourner l'offre lorsqu'elle ne correspond pas au secteur demandé (l'API ne prend pas ce paramètre en entrée, il faut filtrer à postériori)" in {
      // Given
      when(criteresRechercheOffre.secteursActivites) thenReturn List(CodeSecteurActivite("K"))
      when(offreResponse.romeCode) thenReturn Some("H2102")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'offre lorsqu'aucun secteur n'est demandé" in {
      // Given
      when(criteresRechercheOffre.secteursActivites) thenReturn Nil
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "retourner l'offre lorsqu'elle correspond au secteur demandé" in {
      // Given
      when(criteresRechercheOffre.secteursActivites) thenReturn List(CodeSecteurActivite("K"))
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "retourner l'offre lorsqu'elle correspond à l'un des secteurs demandés" in {
      // Given
      when(criteresRechercheOffre.secteursActivites) thenReturn List(
        CodeSecteurActivite("H"),
        CodeSecteurActivite("K")
      )
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "ne pas retourner l'offre lorsqu'elle ne correspond pas à un domaine demandé (l'API ne prend pas ce paramètre en entrée, il faut filtrer à postériori)" in {
      // Given
      when(criteresRechercheOffre.codesDomaineProfessionnels) thenReturn List(CodeDomaineProfessionnel("B18"))
      when(offreResponse.romeCode) thenReturn Some("A1401")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'offre lorsqu'aucun domaine n'est demandé" in {
      // Given
      when(criteresRechercheOffre.codesDomaineProfessionnels) thenReturn Nil

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "retourner l'offre lorsqu'elle correspond au domaine professionnel demandé" in {
      // Given
      when(criteresRechercheOffre.codesDomaineProfessionnels) thenReturn List(CodeDomaineProfessionnel("B18"))
      when(offreResponse.romeCode) thenReturn Some("B1802")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "retourner l'offre lorsqu'elle correspond à l'un des domaines professionnels demandés" in {
      // Given
      when(criteresRechercheOffre.codesDomaineProfessionnels) thenReturn List(
        CodeDomaineProfessionnel("A14"),
        CodeDomaineProfessionnel("B18")
      )
      when(offreResponse.romeCode) thenReturn Some("B1802")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "ne pas retourner l'offre lorsqu'elle ne correspond pas au codeROME demandé (l'API prend ce paramètre en entrée mais seulement 3 code maximum, on préfère filtrer à postériori)" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(CodeROME("A1401"))
      when(offreResponse.romeCode) thenReturn Some("B1802")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'offre lorsqu'aucun codeROME n'est demandé" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn Nil
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "retourner l'offre lorsqu'elle correspond au codeROME demandé" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(CodeROME("K1302"))
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "retourner l'offre lorsqu'elle correspond à l'un des codeROME demandés" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(
        CodeROME("A1401"),
        CodeROME("K1302")
      )
      when(offreResponse.romeCode) thenReturn Some("K1302")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result mustBe List(offreResponse)
    }
    "ne pas retourner l'offre lorsqu'elle correspond au codeROME N41 (trop d'offres avec ce code exigent des permis et des qualifications)" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(
        CodeROME("A1401"),
        CodeROME("N4103")
      )
      when(offreResponse.romeCode) thenReturn Some("N4103")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner l'offre lorsqu'elle correspond au codeROME K2110" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(
        CodeROME("A1401"),
        CodeROME("K2110")
      )
      when(offreResponse.romeCode) thenReturn Some("K2110")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner l'offre lorsqu'elle correspond au codeROME K2503" in {
      // Given
      when(criteresRechercheOffre.codesROME) thenReturn List(
        CodeROME("A1401"),
        CodeROME("K2503")
      )
      when(offreResponse.romeCode) thenReturn Some("K2503")

      // When
      val result = mapping.filterOffresResponses(criteresRechercheOffre, List(offreResponse))

      // Then
      result.isEmpty mustBe true
    }
    "buildPageOffres" should {
      "ne pas retourner de page suivante lorsqu'aucun range n'est retourné" in {
        // Given
        val contentRange = None
        val acceptRange = None

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.isEmpty mustBe true
      }
      "ne pas retourner de page suivante lorsqu'aucun acceptRange n'est retourné" in {
        // Given
        val contentRange = Some("offres 0-149/150")
        val acceptRange = None

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.isEmpty mustBe true
      }
      "ne pas retourner de page suivante lorsqu'aucun contentRange n'est retourné" in {
        // Given
        val contentRange = None
        val acceptRange = Some("150")

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.isEmpty mustBe true
      }
      "ne pas retourner de page suivante lorsque le nombre d'offres total ne dépasse pas la range" in {
        // Given
        val contentRange = Some("offres 0-149/150")
        val acceptRange = Some("150")

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.isEmpty mustBe true
      }
      "ne pas retourner de page suivante lorsque l'indice de départ maximal est atteint" in {
        // Given
        val contentRange = Some("offres 1000-1149/5459")
        val acceptRange = Some("150")

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.isEmpty mustBe true
      }
      "renvoyer une erreur lorsque acceptRange n'est pas un nombre (l'erreur doit remonter pour pouvoir fixer rapidement)" in {
        // Given
        val contentRange = Some("offres 1000-1149/5459")
        val acceptRange = Some("AcceptRange")

        // When & Then
        intercept[NumberFormatException](
          mapping.buildPageOffres(contentRange, acceptRange)
        )
      }
      "retourner une page suivante lorsqu'on reçoit le premier intervalle d'offres" in {
        // Given
        val contentRange = Some("offres 0-149/5459")
        val acceptRange = Some("150")

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.contains(PageOffres(150, 299)) mustBe true
      }
      "retourner une page suivante lorsqu'on reçoit un intervalle" in {
        // Given
        val contentRange = Some("offres 300-449/3011")
        val acceptRange = Some("150")

        // When
        val result = mapping.buildPageOffres(contentRange, acceptRange)

        // Then
        result.contains(PageOffres(450, 599)) mustBe true
      }
    }
    "buildOffre" should {
      "ne pas modifier l'URL du logo de l'entreprise si elle existe déjà" in {
        // Given
        when(offreResponse.logoEntreprise) thenReturn Some("https://entreprise/static/logo.png")

        // When
        val result = mapping.buildOffre(offreResponse)

        // Then
        result.entreprise.urlLogo mustBe Some("https://entreprise/static/logo.png")
      }
      "intégrer l'URL de base devant le logo de l'entreprise et rajouter l'extension png si aucune URL n'est fournie" in {
        // Given
        when(offreResponse.logoEntreprise) thenReturn Some("logo")

        // When
        val result = mapping.buildOffre(offreResponse)

        // Then
        result.entreprise.urlLogo mustBe Some("https://entreprise.pole-emploi.fr/static/img/logos/logo.png")
      }
    }
  }
}
