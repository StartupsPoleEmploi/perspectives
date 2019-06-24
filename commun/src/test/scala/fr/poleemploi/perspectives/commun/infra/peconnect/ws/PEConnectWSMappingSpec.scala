package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

import fr.poleemploi.perspectives.candidat._
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
  "buildPermis" should {
    "ne pas renvoyer de permis s'il n'a pas de code" in {
      // Given
      val permisResponse = PermisResponse(
        code = None,
        libelle = ""
      )

      // When
      val result = mapping.buildPermis(List(permisResponse))

      // Then
      result.isEmpty mustBe true
    }
    "renvoyer la liste des permis sans le code répété dans le label" in {
      // Given
      val permisResponse = PermisResponse(
        code = Some("B"),
        libelle = "B - Véhicule léger"
      )

      // When
      val result = mapping.buildPermis(List(permisResponse))

      // Then
      result.size mustBe 1
      result.head mustBe Permis(code = "B", label = "Véhicule léger")
    }
  }
  "buildLangues" should {
    "renvoyer une langue sans niveau lorsqu'il vaut 0 (même si ce cas n'est pas précisé dans la doc de l'emploi store)" in {
      // Given
      val langueResponse = LangueResponse(
        code = Some("FR"),
        libelle = "Français",
        niveau = Some(NiveauLangueResponse("0", libelle = "Aucun"))
      )

      // When
      val result = mapping.buildLanguesCandidat(List(langueResponse))

      // Then
      result.size mustBe 1
      result.head mustBe Langue(label = "Français", niveau = None)
    }
    "renvoyer une langue" in {
      // Given
      val langueResponse = LangueResponse(
        code = Some("FR"),
        libelle = "Français",
        niveau = Some(NiveauLangueResponse("1", libelle = "Avancé"))
      )

      // When
      val result = mapping.buildLanguesCandidat(List(langueResponse))

      // Then
      result.size mustBe 1
      result.head mustBe Langue(label = "Français", niveau = Some(NiveauLangue.DEBUTANT))
    }
  }
  "buildFormations" should {
    "ne pas retourner de formation lorsque la réponse de l'API ne comporte pas l'année de fin" in {
      // Given
      val formationResponse = FormationResponse(
        anneeFin = None,
        description = None,
        diplomeObtenu = false,
        etranger = false,
        intitule = Some("Boucher"),
        lieu = None,
        niveau = None,
        domaine = None
      )

      // When
      val result = mapping.buildFormations(List(formationResponse))

      // Then
      result.size mustBe 0
    }
    "ne pas retourner de formation lorsque la réponse de l'API ne comporte pas d'intitulé (même si le champ est marqué obligatoire dans la doc de l'emploi store)" in {
      // Given
      val formationResponse = FormationResponse(
        anneeFin = Some(LocalDate.now().getYear),
        description = None,
        diplomeObtenu = false,
        etranger = false,
        intitule = None,
        lieu = None,
        niveau = None,
        domaine = None
      )

      // When
      val result = mapping.buildFormations(List(formationResponse))

      // Then
      result.size mustBe 0
    }
    "retourner les informations de la formation lorsqu'elle comporte un intitulé et une date de fin" in {
      // Given
      val formationResponse = FormationResponse(
        anneeFin = Some(LocalDate.now().getYear),
        description = None,
        diplomeObtenu = false,
        etranger = false,
        intitule = Some("Développeur"),
        lieu = Some("Paris"),
        niveau = Some(NiveauFormationResponse(code = "", libelle = "Bac+5")),
        domaine = Some(DomaineFormationResponse(code = "", libelle = "Informatique de gestion"))
      )

      // When
      val result = mapping.buildFormations(List(formationResponse))

      // Then
      result.size mustBe 1
      result.head mustBe Formation(
        anneeFin = LocalDate.now().getYear,
        intitule = "Développeur",
        lieu = Some("Paris"),
        domaine = Some(DomaineFormation("Informatique de gestion")),
        niveau = Some(NiveauFormation("Bac+5"))
      )
    }
  }
  "buildExperienceProfessionnelles" should {
    "ne pas retourner d'expérience lorsque la réponse de l'API ne comporte pas la date de début" in {
      // Given
      val experienceProfessionnelleResponse = ExperienceProfessionnelleResponse(
        date = None,
        description = None,
        enPoste = false,
        etranger = false,
        intitule = Some("Assistante Maternelle Agréée à domicile"),
        lieu = None,
        duree = None,
        entreprise = None
      )

      // When
      val result = mapping.buildExperienceProfessionnelles(List(experienceProfessionnelleResponse))

      // Then
      result.size mustBe 0
    }
    "ne pas retourner d'expérience lorsque la réponse de l'API ne comporte pas d'intitulé (même si le champ est marqué obligatoire dans la doc de l'emploi store)" in {
      // Given
      val experienceProfessionnelleResponse = ExperienceProfessionnelleResponse(
        date = Some(DateExperienceProfessionnelleResponse(
          debut = Some(LocalDateTime.now().minusYears(5L)),
          fin = None
        )),
        description = None,
        enPoste = false,
        etranger = false,
        intitule = None,
        lieu = None,
        duree = None,
        entreprise = None
      )

      // When
      val result = mapping.buildExperienceProfessionnelles(List(experienceProfessionnelleResponse))

      // Then
      result.size mustBe 0
    }
    "retourner les informations de l'expérience lorsqu'elle comporte un intitulé et une date de début" in {
      // Given
      val dateDebut = LocalDateTime.now().minusYears(5L)
      val experienceProfessionnelleResponse = ExperienceProfessionnelleResponse(
        date = Some(DateExperienceProfessionnelleResponse(
          debut = Some(dateDebut),
          fin = None
        )),
        description = None,
        enPoste = false,
        etranger = false,
        intitule = Some("Assistante Maternelle Agréée à domicile"),
        lieu = None,
        duree = None,
        entreprise = None
      )

      // When
      val result = mapping.buildExperienceProfessionnelles(List(experienceProfessionnelleResponse))

      // Then
      result.size mustBe 1
      result.head mustBe ExperienceProfessionnelle(
        dateDebut = dateDebut.toLocalDate,
        dateFin = None,
        enPoste = false,
        intitule = "Assistante Maternelle Agréée à domicile",
        nomEntreprise = None,
        lieu = None,
        description = None
      )
    }
  }
}
