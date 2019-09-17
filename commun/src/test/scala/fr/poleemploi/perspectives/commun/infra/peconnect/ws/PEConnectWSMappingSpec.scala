package fr.poleemploi.perspectives.commun.infra.peconnect.ws

import java.time.{LocalDateTime, ZonedDateTime}

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class PEConnectWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val mapping = new PEConnectWSMapping

  "buildAdresse" should {
    "ne pas retourner d'adresse lorsque la réponse ne comporte pas le champ adresse4" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.adresse4) thenReturn None

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner d'adresse lorsque la réponse  ne comporte pas le champ codePostal" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.codePostal) thenReturn None

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner d'adresse lorsque la réponse ne comporte pas le champ libelleCommune" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.libelleCommune) thenReturn None

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner d'adresse lorsque la réponse ne comporte pas le champ libellePays" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.libellePays) thenReturn None

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.isEmpty mustBe true
    }
    "retourner la voie de l'adresse" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.adresse4) thenReturn Some("rue des oursons")

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.exists(_.voie == "rue des oursons") mustBe true
    }
    "retourner le code postal de l'adresse" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.codePostal) thenReturn Some("75011")

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.exists(_.codePostal == "75011") mustBe true
    }
    "retourner la commune de l'adresse" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.libelleCommune) thenReturn Some("Paris")

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.exists(_.libelleCommune == "Paris") mustBe true
    }
    "retourner le pays de l'adresse" in {
      // Given
      val response = mockCoordonneesCandidatResponseValide
      when(response.libellePays) thenReturn Some("France")

      // When
      val result = mapping.buildAdresse(response)

      // Then
      result.exists(_.libellePays == "France") mustBe true
    }
  }
  "buildMRSValidees" should {
    "ne pas retourner de MRS lorsque la reponse ne contient aucun codeResultat" in {
      // Given
      val response = mockResultatRendezVousResponseValide
      when(response.listeCodeResultat) thenReturn None

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner de MRS lorsque la reponse ne contient aucun codeSitePESuiviResultat" in {
      // Given
      val response = mockResultatRendezVousResponseValide
      when(response.codeSitePESuiviResultat) thenReturn None

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner de MRS lorsque la reponse contient pas un codeResultat representant une MRS non validée" in {
      // Given
      val response = mockResultatRendezVousResponseValide
      when(response.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse("NON_VALIDEE")))

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "retourner une MRSValidee avec le CodeROME" in {
      // Given
      val response = mockResultatRendezVousResponseValide
      when(response.codeRome) thenReturn "A1401"

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.exists(_.codeROME == CodeROME("A1401")) mustBe true
    }
    "retourner une MRSValidee avec le CodeDepartement" in {
      // Given
      val response = mockResultatRendezVousResponseValide
      when(response.codeSitePESuiviResultat) thenReturn Some("33201")

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.exists(_.codeDepartement == CodeDepartement("33")) mustBe true
    }
    "retourner une MRSValidee avec la date d'évaluation" in {
      // Given
      val dateMRS = ZonedDateTime.now()
      val response = mockResultatRendezVousResponseValide
      when(response.dateDebutSession) thenReturn dateMRS

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.exists(_.dateEvaluation == dateMRS.toLocalDate) mustBe true
    }
    "retourner une MRSValidee non DHAE" in {
      // Given
      val response = mockResultatRendezVousResponseValide

      // When
      val result = mapping.buildMRSValidees(List(response))

      // Then
      result.exists(_.isDHAE == false) mustBe true
    }
    "retourner les MRSValidee lorsque plusieurs résultats sont retournés avec le même CodeROME et des CodeDepartement différents" in {
      // Given
      val response1 = mockResultatRendezVousResponseValide
      when(response1.codeRome) thenReturn "K2204"
      when(response1.codeSitePESuiviResultat) thenReturn Some("33201")
      val response2 = mockResultatRendezVousResponseValide
      when(response2.codeRome) thenReturn "K2204"
      when(response2.codeSitePESuiviResultat) thenReturn Some("72201")

      // When
      val result = mapping.buildMRSValidees(List(response1, response2))

      // Then
      result.size mustBe 2
    }
    "retourner une seule MRSValidee lorsque plusieurs résultats sont retournés avec le même CodeROME et même CodeDepartement (la saisie a été faite plusieurs fois côté SI PoleEmploi avec un statut différent mais un seul enregistrement nous intéresse : la date importe peu)" in {
      // Given
      val response1 = mockResultatRendezVousResponseValide
      when(response1.codeRome) thenReturn "K2204"
      when(response1.codeSitePESuiviResultat) thenReturn Some("33201")
      val response2 = mockResultatRendezVousResponseValide
      when(response2.codeRome) thenReturn "K2204"
      when(response2.codeSitePESuiviResultat) thenReturn Some("33201")

      // When
      val result = mapping.buildMRSValidees(List(response1, response2))

      // Then
      result.size mustBe 1
    }
  }
  "buildPermis" should {
    "ne pas renvoyer de permis s'il n'a pas de code" in {
      // Given
      val response = mockPermisResponseValide
      when(response.code) thenReturn None

      // When
      val result = mapping.buildPermis(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "renvoyer le code du permis" in {
      // Given
      val response = mockPermisResponseValide
      when(response.code) thenReturn Some("B")

      // When
      val result = mapping.buildPermis(List(response))

      // Then
      result.exists(_.code == "B") mustBe true
    }
    "renvoyer le label du sans le code répété dans le label" in {
      // Given
      val response = mockPermisResponseValide
      when(response.libelle) thenReturn "B - Véhicule léger"

      // When
      val result = mapping.buildPermis(List(response))

      // Then
      result.exists(_.label == "Véhicule léger") mustBe true
    }
  }
  "buildLangues" should {
    "renvoyer une langue sans niveau lorsqu'il vaut 0 (même si ce cas n'est pas précisé dans la doc de l'emploi store)" in {
      // Given
      val response = mockLangueResponseValide
      when(response.niveau) thenReturn Some(NiveauLangueResponse("0", libelle = "Aucun"))

      // When
      val result = mapping.buildLanguesCandidat(List(response))

      // Then
      result.isEmpty mustBe false
    }
    "renvoyer le libellé de la langue" in {
      // Given
      val response = mockLangueResponseValide
      when(response.libelle) thenReturn "Français"

      // When
      val result = mapping.buildLanguesCandidat(List(response))

      // Then
      result.exists(_.label == "Français") mustBe true
    }
    "renvoyer le niveau de la langue" in {
      // Given
      val response = mockLangueResponseValide
      when(response.niveau) thenReturn Some(NiveauLangueResponse("1", libelle = "Débutant"))

      // When
      val result = mapping.buildLanguesCandidat(List(response))

      // Then
      result.exists(_.niveau.contains(NiveauLangue.DEBUTANT)) mustBe true
    }
  }
  "buildFormations" should {
    "ne pas retourner de formation lorsque la réponse ne comporte pas l'année de fin" in {
      // Given
      val response = mockFormationReponseValide
      when(response.anneeFin) thenReturn None

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner de formation lorsque la réponse ne comporte pas d'intitulé (même si le champ est marqué obligatoire dans la doc de l'emploi store)" in {
      // Given
      val response = mockFormationReponseValide
      when(response.intitule) thenReturn None

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'année de fin de la formation" in {
      // Given
      val response = mockFormationReponseValide
      when(response.anneeFin) thenReturn Some(2019)

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.exists(_.anneeFin == 2019) mustBe true
    }
    "retourner l'intitulé de la formation" in {
      // Given
      val response = mockFormationReponseValide
      when(response.intitule) thenReturn Some("Magicien")

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.exists(_.intitule == "Magicien") mustBe true
    }
    "retourner le lieu de la formation" in {
      // Given
      val response = mockFormationReponseValide
      when(response.lieu) thenReturn Some("Paris")

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.exists(_.lieu.contains("Paris")) mustBe true
    }
    "retourner le domaine de la formation" in {
      // Given
      val response = mockFormationReponseValide
      when(response.domaine) thenReturn Some(DomaineFormationResponse(code = "", libelle = "Informatique de gestion"))

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.exists(_.domaine.contains(DomaineFormation("Informatique de gestion"))) mustBe true
    }
    "retourner le niveau de la formation" in {
      // Given
      val response = mockFormationReponseValide
      when(response.niveau) thenReturn Some(NiveauFormationResponse(code = "", libelle = "Bac+5"))

      // When
      val result = mapping.buildFormations(List(response))

      // Then
      result.exists(_.niveau.contains(NiveauFormation("Bac+5"))) mustBe true
    }
  }
  "buildExperienceProfessionnelles" should {
    "ne pas retourner d'expérience lorsque la réponse ne comporte pas de date" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.date) thenReturn None

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner d'expérience lorsque la réponse ne comporte pas la date de début" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.date) thenReturn Some(DateExperienceProfessionnelleResponse(
        debut = None, fin = None
      ))

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "ne pas retourner d'expérience lorsque la réponse ne comporte pas d'intitulé (même si le champ est marqué obligatoire dans la doc de l'emploi store)" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.intitule) thenReturn None

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "retourner l'intitule de l'experience" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.intitule) thenReturn Some("Magicien")

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.intitule == "Magicien") mustBe true
    }
    "retourner la date de début de l'experience" in {
      // Given
      val dateDebut = LocalDateTime.now().minusYears(1L)
      val response = mockExperienceProfessionnelleResponseValide
      when(response.date) thenReturn Some(DateExperienceProfessionnelleResponse(
        debut = Some(dateDebut), fin = None
      ))

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.dateDebut == dateDebut.toLocalDate) mustBe true
    }
    "retourner la date de fin de l'experience" in {
      // Given
      val dateFin = LocalDateTime.now()
      val response = mockExperienceProfessionnelleResponseValide
      when(response.date) thenReturn Some(DateExperienceProfessionnelleResponse(
        debut = Some(LocalDateTime.now().minusYears(1L)),
        fin = Some(dateFin)
      ))

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.dateFin.contains(dateFin.toLocalDate)) mustBe true
    }
    "retourner le champ enPoste de l'experience" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.enPoste) thenReturn true

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.enPoste) mustBe true
    }
    "retourner le nom de l'entreprise de l'experience" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.entreprise) thenReturn Some("PlacotBat")

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.nomEntreprise.contains("PlacotBat")) mustBe true
    }
    "retourner le lieu de l'experience" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.lieu) thenReturn Some("Paris")

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.lieu.contains("Paris")) mustBe true
    }
    "retourner la description de l'experience" in {
      // Given
      val response = mockExperienceProfessionnelleResponseValide
      when(response.description) thenReturn Some("description")

      // When
      val result = mapping.buildExperienceProfessionnelles(List(response))

      // Then
      result.exists(_.description.contains("description")) mustBe true
    }
  }
  "buildSavoirEtreProfessionnels" should {
    "ne pas retourner de savoirEtre lorsque la réponse ne comporte pas de libelle (même si le champ est marqué obligatoire dans la doc de l'emploi store)" in {
      // Given
      val response = mockSavoirEtreResponseValide
      when(response.libelle) thenReturn None

      // When
      val result = mapping.buildSavoirEtreProfessionnels(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "retourner le savoirEtre" in {
      // Given
      val response = mockSavoirEtreResponseValide
      when(response.libelle) thenReturn Some("Rigoureux")

      // When
      val result = mapping.buildSavoirEtreProfessionnels(List(response))

      // Then
      result.contains(SavoirEtre("Rigoureux")) mustBe true
    }
  }
  "buildSavoirFaire" should {
    "ne pas retourner de savoirEtre lorsque la réponse ne comporte pas de libelle (même si le champ est marqué obligatoire dans la doc de l'emploi store)" in {
      // Given
      val response = mockSavoirFaireResponseValide
      when(response.libelle) thenReturn None

      // When
      val result = mapping.buildSavoirFaire(List(response))

      // Then
      result.isEmpty mustBe true
    }
    "retourner le label du savoirFaire" in {
      // Given
      val response = mockSavoirFaireResponseValide
      when(response.libelle) thenReturn Some("Découper du jambon")

      // When
      val result = mapping.buildSavoirFaire(List(response))

      // Then
      result.exists(_.label == "Découper du jambon") mustBe true
    }
    "retourner le niveau du savoirFaire" in {
      // Given
      val response = mockSavoirFaireResponseValide
      when(response.niveau) thenReturn Some(NiveauCompetenceResponse(code = "1", libelle = ""))

      // When
      val result = mapping.buildSavoirFaire(List(response))

      // Then
      result.exists(_.niveau.contains(NiveauSavoirFaire.DEBUTANT)) mustBe true
    }
  }
  "buildPEConnectRecruteurInfos" should {
    "renvoyer non certifie quand l'habilitation est vide" in {
      // Given
      val response = mockRecruteurInfosResponseValide
      when(response.habilitation) thenReturn None

      // When
      val result = mapping.buildPEConnectRecruteurInfos(response)

      // Then
      result.certifie mustBe false
    }
    "renvoyer non certifie quand l'habilitation ne contient pas la valeur recruteurcertifie" in {
      // Given
      val response = mockRecruteurInfosResponseValide
      when(response.habilitation) thenReturn Some("noncertifie")

      // When
      val result = mapping.buildPEConnectRecruteurInfos(response)

      // Then
      result.certifie mustBe false
    }
    "renvoyer certifie quand l'habilitation contient la valeur recruteurcertifie" in {
      // Given
      val response = mockRecruteurInfosResponseValide
      when(response.habilitation) thenReturn Some("recruteurcertifie")

      // When
      val result = mapping.buildPEConnectRecruteurInfos(response)

      // Then
      result.certifie mustBe true
    }
  }
  "buildPEConnectRecruteurInfosAlternative" should {
    "renvoyer non certifie quand la liste d'habilitations est vide" in {
      // Given
      val response = mockRecruteurInfosAlternativeResponseValide
      when(response.habilitation) thenReturn None

      // When
      val result = mapping.buildPEConnectRecruteurInfosAlternative(response)

      // Then
      result.certifie mustBe false
    }
    "renvoyer non certifie quand la liste d'habilitations ne contient pas la valeur recruteurcertifie" in {
      // Given
      val response = mockRecruteurInfosAlternativeResponseValide
      when(response.habilitation) thenReturn Some(Seq("administrateur", "noncertifie"))

      // When
      val result = mapping.buildPEConnectRecruteurInfosAlternative(response)

      // Then
      result.certifie mustBe false
    }
    "renvoyer certifie quand la liste d'habilitations contient la valeur recruteurcertifie" in {
      // Given
      val response = mockRecruteurInfosAlternativeResponseValide
      when(response.habilitation) thenReturn Some(Seq("administrateur", "recruteurcertifie"))

      // When
      val result = mapping.buildPEConnectRecruteurInfosAlternative(response)

      // Then
      result.certifie mustBe true
    }
  }

  private def mockResultatRendezVousResponseValide: ResultatRendezVousResponse = {
    val response = mock[ResultatRendezVousResponse]
    when(response.codeRome) thenReturn "K2204"
    when(response.codeSitePESuiviResultat) thenReturn Some("33201")
    when(response.listeCodeResultat) thenReturn Some(List(CodeResultatRendezVousResponse.VALIDE))
    when(response.dateDebutSession) thenReturn ZonedDateTime.now()
    response
  }

  private def mockCoordonneesCandidatResponseValide: CoordonneesCandidatReponse = {
    val response = mock[CoordonneesCandidatReponse]
    when(response.adresse1) thenReturn None
    when(response.adresse2) thenReturn None
    when(response.adresse2) thenReturn None
    when(response.adresse4) thenReturn Some("rue des pivoines")
    when(response.codePostal) thenReturn Some("75011")
    when(response.codeINSEE) thenReturn Some("75000")
    when(response.libelleCommune) thenReturn Some("Paris")
    when(response.codePays) thenReturn None
    when(response.libellePays) thenReturn Some("France")
    response
  }

  private def mockPermisResponseValide: PermisResponse = {
    val response = mock[PermisResponse]
    when(response.code) thenReturn Some("B")
    when(response.libelle) thenReturn "B - Véhicule léger"
    response
  }

  private def mockLangueResponseValide: LangueResponse = {
    val response = mock[LangueResponse]
    when(response.code) thenReturn Some("FR")
    when(response.libelle) thenReturn "Français"
    when(response.niveau) thenReturn Some(NiveauLangueResponse("1", libelle = "Débutant"))
    response
  }


  private def mockFormationReponseValide: FormationResponse = {
    val response = mock[FormationResponse]
    when(response.anneeFin) thenReturn Some(2019)
    when(response.description) thenReturn None
    when(response.diplomeObtenu) thenReturn false
    when(response.etranger) thenReturn false
    when(response.intitule) thenReturn Some("Boucher")
    when(response.niveau) thenReturn None
    when(response.domaine) thenReturn None
    when(response.lieu) thenReturn None
    response
  }

  private def mockExperienceProfessionnelleResponseValide: ExperienceProfessionnelleResponse = {
    val response = mock[ExperienceProfessionnelleResponse]
    when(response.intitule) thenReturn Some("Assistante Maternelle Agréée à domicile")
    when(response.date) thenReturn Some(DateExperienceProfessionnelleResponse(
      debut = Some(LocalDateTime.now()),
      fin = None
    ))
    when(response.enPoste) thenReturn false
    when(response.entreprise) thenReturn None
    when(response.lieu) thenReturn None
    when(response.description) thenReturn None
    response
  }

  private def mockSavoirEtreResponseValide: CompetenceResponse = {
    val response = mock[CompetenceResponse]
    when(response.libelle) thenReturn Some("Rigoureux")
    when(response.typeCompetence) thenReturn TypeCompetenceResponse.SAVOIR_ETRE
    response
  }

  private def mockSavoirFaireResponseValide: CompetenceResponse = {
    val response = mock[CompetenceResponse]
    when(response.libelle) thenReturn Some("Réaliser une opération d'affûtage")
    when(response.typeCompetence) thenReturn TypeCompetenceResponse.SAVOIR_FAIRE_METIER
    when(response.niveau) thenReturn None
    response
  }

  private def mockRecruteurInfosResponseValide: UserInfosEntrepriseResponse = {
    val response = mock[UserInfosEntrepriseResponse]
    when(response.email) thenReturn "test@pole-emploi.fr"
    when(response.sub) thenReturn "test"
    when(response.familyName) thenReturn "Patulacci"
    when(response.givenName) thenReturn "Marcel"
    when(response.gender) thenReturn "male"
    when(response.habilitation) thenReturn Some("recruteurcertifie")
    response
  }

  private def mockRecruteurInfosAlternativeResponseValide: UserInfosEntrepriseAlternativeResponse = {
    val response = mock[UserInfosEntrepriseAlternativeResponse]
    when(response.email) thenReturn "test@pole-emploi.fr"
    when(response.sub) thenReturn "test"
    when(response.familyName) thenReturn "Patulacci"
    when(response.givenName) thenReturn "Marcel"
    when(response.gender) thenReturn "male"
    when(response.habilitation) thenReturn Some(Seq("recruteurcertifie"))
    response
  }
}
