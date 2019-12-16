package fr.poleemploi.perspectives.emailing.infra.ws

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.authentification.infra.autologin.JwtToken
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, _}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, MRSProspectCandidat, MRSValideeCandidat, OffreGereeParRecruteurAvecCandidats}
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSMapping._
import fr.poleemploi.perspectives.metier.domain.Metier
import fr.poleemploi.perspectives.offre.domain.OffreId
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class MailjetWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactProperties._

  var mapping: MailjetWSMapping = _

  var candidatInscrit: CandidatInscrit = _
  var mrsValideeCandidat: MRSValideeCandidat = _

  val templateId = 12345
  val baseUrl = "https://perspectives.pole-emploi.fr"
  val idListe = 12345

  before {
    candidatInscrit = mock[CandidatInscrit]
    when(candidatInscrit.email) thenReturn Email("nom.prenom@mail.com")
    when(candidatInscrit.nom) thenReturn Nom("Nom")
    when(candidatInscrit.prenom) thenReturn Prenom("Prenom")
    when(candidatInscrit.genre) thenReturn Genre.HOMME

    mrsValideeCandidat = mock[MRSValideeCandidat]
    when(mrsValideeCandidat.metier) thenReturn Metier(
      codeROME = CodeROME("A1401"),
      label = "Agriculteur"
    )
    when(mrsValideeCandidat.dateEvaluation) thenReturn LocalDate.now()

    mapping = new MailjetWSMapping
  }

  "buildContactRequestInscriptionCandidat" should {
    "construire une requete avec la propriété cv à false" in {
      // When
      val request = mapping.buildContactRequestInscriptionCandidat(candidatInscrit)

      // Then
      (request.properties \ cv).as[Boolean] mustBe false
    }
  }
  "buildRequestMiseAJourMRSValideeCandidat" should {
    "construire une requete avec le code ROME de la MRS" in {
      // When
      val request = mapping.buildRequestMiseAJourMRSValideeCandidat(mrsValideeCandidat)

      // Then
      request.properties.exists(p =>
       p.name == mrs_code_rome && p.value == mrsValideeCandidat.metier.codeROME.value
      ) mustBe true
    }
    "construire une requete avec le label du métier de la MRS" in {
      // When
      val request = mapping.buildRequestMiseAJourMRSValideeCandidat(mrsValideeCandidat)

      // Then
      request.properties.exists(p =>
        p.name == mrs_metier && p.value == mrsValideeCandidat.metier.label
      ) mustBe true
    }
    "construire une requete avec la date de la MRS formattée" in {
      // Given
      val dateEvaluation = LocalDate.now().minusYears(1L)
      when(mrsValideeCandidat.dateEvaluation) thenReturn dateEvaluation

      // When
      val request = mapping.buildRequestMiseAJourMRSValideeCandidat(mrsValideeCandidat)

      // Then
      request.properties.exists(p =>
        p.name == mrs_date && p.value == DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateEvaluation.atStartOfDay())
      ) mustBe true
    }
  }
  "buildRequestImportProspectsCandidats" should {
    "construire une requete avec l'identifiant PEConnect" in {
      // Given
      val prospect = mockMRSProspectCandidat

      // When
      val request = mapping.buildRequestImportProspectsCandidats(idListe, Stream(prospect))

      // Then
      (request.contacts.head.properties \ id_peconnect).as[String] mustBe prospect.peConnectId.value
    }
    "construire une requete avec l'identifiant local" in {
      // Given
      val prospect = mockMRSProspectCandidat

      // When
      val request = mapping.buildRequestImportProspectsCandidats(idListe, Stream(prospect))

      // Then
      (request.contacts.head.properties \ identifiant_local).as[String] mustBe prospect.identifiantLocal.value
    }
    "construire une requete avec le code neptune" in {
      // Given
      val prospect = mockMRSProspectCandidat

      // When
      val request = mapping.buildRequestImportProspectsCandidats(idListe, Stream(prospect))

      // Then
      (request.contacts.head.properties \ code_neptune).as[String] mustBe prospect.codeNeptune.value
    }
  }
  "buildRequestCandidatsPourOffreEnDifficulteGereeParRecruteur" should {
    "construire une requete avec la campagne GA de la version A du template" in {
      // Given & When
      val request = mapping.buildRequestCandidatsPourOffreEnDifficulteGereeParRecruteur(baseUrl, templateId, useVersionA = true, Seq(mockOffreGereeParRecruteurAvecCandidats))

      // Then
      request.messages.head.variables.getOrElse(VAR_URL_RECHERCHE_CANDIDATS, "") must include ("&utm_campaign=offre-en-difficulte-sans-preselection-version-a")
    }
    "construire une requete avec la campagne GA de la version B du template" in {
      // Given & When
      val request = mapping.buildRequestCandidatsPourOffreEnDifficulteGereeParRecruteur(baseUrl, templateId, useVersionA = false, Seq(mockOffreGereeParRecruteurAvecCandidats))

      // Then
      request.messages.head.variables.getOrElse(VAR_URL_RECHERCHE_CANDIDATS, "") must include ("&utm_campaign=offre-en-difficulte-sans-preselection-version-b")
    }
    "construire une requete avec la campagne mailjet de la version A du template" in {
      // Given & When
      val request = mapping.buildRequestCandidatsPourOffreEnDifficulteGereeParRecruteur(baseUrl, templateId, useVersionA = true, Seq(mockOffreGereeParRecruteurAvecCandidats))

      // Then
      request.messages.head.category.getOrElse("") mustBe "offre_en_difficulte_geree_par_recruteur_version_a"
    }
    "construire une requete avec la campagne mailjet de la version B du template" in {
      // Given & When
      val request = mapping.buildRequestCandidatsPourOffreEnDifficulteGereeParRecruteur(baseUrl, templateId, useVersionA = false, Seq(mockOffreGereeParRecruteurAvecCandidats))

      // Then
      request.messages.head.category.getOrElse("") mustBe "offre_en_difficulte_geree_par_recruteur_version_b"
    }
  }
  "buildRequestEmailDisponibiliteCandidat" should {
    "construire une requete avec l'id du template mailjet" in {
      // Given
      val candidats = Stream(mockEmailingDisponibiliteCandidatAvecEmail)

      // When
      val request = mapping.buildRequestEmailDisponibiliteCandidat(baseUrl, templateId, candidats)

      // Then
      request.messages.head.templateId mustBe templateId
    }
    "construire une requete avec la propriete templateLanguage a true" in {
      // Given
      val candidats = Stream(mockEmailingDisponibiliteCandidatAvecEmail)

      // When
      val request = mapping.buildRequestEmailDisponibiliteCandidat(baseUrl, templateId, candidats)

      // Then
      request.messages.head.templateLanguage mustBe true
    }
    "construire une requete avec la categorie specifique" in {
      // Given
      val candidats = Stream(mockEmailingDisponibiliteCandidatAvecEmail)

      // When
      val request = mapping.buildRequestEmailDisponibiliteCandidat(baseUrl, templateId, candidats)

      // Then
      request.messages.head.category mustBe Some(DISPONIBILITE_CANDIDAT_CATEGORY)
    }
    "construire une requete avec le mail du candidat en destinataire" in {
      // Given
      val candidats = Stream(mockEmailingDisponibiliteCandidatAvecEmail)

      // When
      val request = mapping.buildRequestEmailDisponibiliteCandidat(baseUrl, templateId, candidats)

      // Then
      request.messages.head.to.head.email mustBe "email@candidat.fr"
    }
    "construire une requete avec les variables mailjet contenant les URLs autologuees vers le formulaire de disponibilites" in {
      // Given
      val candidats = Stream(mockEmailingDisponibiliteCandidatAvecEmail)

      // When
      val request = mapping.buildRequestEmailDisponibiliteCandidat(baseUrl, templateId, candidats)

      // Then
      request.messages.head.variables.get(VAR_URL_FORMULAIRE_DISPO_CANDIDAT_EN_RECHERCHE) mustBe Some("https://perspectives.pole-emploi.fr/candidat/disponibilites?candidatEnRecherche=true&token=token")
      request.messages.head.variables.get(VAR_URL_FORMULAIRE_DISPO_CANDIDAT_PAS_EN_RECHERCHE) mustBe Some("https://perspectives.pole-emploi.fr/candidat/disponibilites?candidatEnRecherche=false&token=token")
    }
  }

  private def mockMRSProspectCandidat: MRSProspectCandidat = {
    val prospect = mock[MRSProspectCandidat]
    when(prospect.email) thenReturn Email("nom.prenom@mail.com")
    when(prospect.nom) thenReturn Nom("Nom")
    when(prospect.prenom) thenReturn Prenom("Prenom")
    when(prospect.genre) thenReturn Genre.HOMME
    when(prospect.codeDepartement) thenReturn CodeDepartement("69")
    when(prospect.metier) thenReturn Metier(
      codeROME = CodeROME("A1401"),
      label = "Agriculteur"
    )
    when(prospect.dateEvaluation) thenReturn LocalDate.now()
    when(prospect.peConnectId) thenReturn PEConnectId("28d0b75a-b694-4de3-8849-18bfbfebd729")
    when(prospect.identifiantLocal) thenReturn IdentifiantLocal("0123456789A")
    when(prospect.codeNeptune) thenReturn CodeNeptune("IADE3110")
    prospect
  }

  private def mockEmailingDisponibiliteCandidatAvecEmail: EmailingDisponibiliteCandidatAvecEmail = {
    val candidat = mock[EmailingDisponibiliteCandidatAvecEmail]
    when(candidat.email).thenReturn(Email("email@candidat.fr"))
    when(candidat.autologinToken).thenReturn(JwtToken("token"))
    candidat
  }

  private def mockOffreGereeParRecruteurAvecCandidats: OffreGereeParRecruteurAvecCandidats = {
    val offre = mock[OffreGereeParRecruteurAvecCandidats]
    when(offre.emailCorrespondant).thenReturn(Email("email@candidat.fr"))
    when(offre.intitule).thenReturn("Mon offre")
    when(offre.offreId).thenReturn(OffreId("123456789"))
    when(offre.datePublication).thenReturn(LocalDate.now)
    when(offre.codeROME).thenReturn(CodeROME("M1601"))
    val coordonnees = mock[Coordonnees]
    when(coordonnees.latitude).thenReturn(48.8534)
    when(coordonnees.longitude).thenReturn(2.3488)
    when(offre.coordonnees).thenReturn(coordonnees)
    when(offre.lieuTravail).thenReturn("Paris")
    offre
  }
}
