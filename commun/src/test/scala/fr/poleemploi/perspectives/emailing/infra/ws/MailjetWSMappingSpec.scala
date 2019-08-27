package fr.poleemploi.perspectives.emailing.infra.ws

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.authentification.infra.autologin.JwtToken
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, MRSValideeCandidat}
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSMapping._
import fr.poleemploi.perspectives.metier.domain.Metier
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

  private def mockEmailingDisponibiliteCandidatAvecEmail: EmailingDisponibiliteCandidatAvecEmail = {
    val candidat = mock[EmailingDisponibiliteCandidatAvecEmail]
    when(candidat.email).thenReturn(Email("email@candidat.fr"))
    when(candidat.autologinToken).thenReturn(JwtToken("token"))
    candidat
  }
}
