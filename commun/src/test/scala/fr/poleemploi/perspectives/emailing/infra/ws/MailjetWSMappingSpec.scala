package fr.poleemploi.perspectives.emailing.infra.ws

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, MRSValideeCandidat, RecruteurInscrit}
import fr.poleemploi.perspectives.metier.domain.Metier
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class MailjetWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactProperties._

  var mapping: MailjetWSMapping = _

  var candidatInscrit: CandidatInscrit = _
  var recruteurInscrit: RecruteurInscrit = _
  var mrsValideeCandidat: MRSValideeCandidat = _

  before {
    candidatInscrit = mock[CandidatInscrit]
    when(candidatInscrit.email) thenReturn Email("nom.prenom@mail.com")
    when(candidatInscrit.nom) thenReturn Nom("Nom")
    when(candidatInscrit.prenom) thenReturn Prenom("Prenom")
    when(candidatInscrit.genre) thenReturn Genre.HOMME

    recruteurInscrit = mock[RecruteurInscrit]
    when(recruteurInscrit.email) thenReturn Email("nom.prenom@mail.com")
    when(recruteurInscrit.nom) thenReturn Nom("Nom")
    when(recruteurInscrit.prenom) thenReturn Prenom("Prenom")
    when(recruteurInscrit.genre) thenReturn Genre.HOMME

    mrsValideeCandidat = mock[MRSValideeCandidat]
    when(mrsValideeCandidat.metier) thenReturn Metier(
      codeROME = CodeROME("A1401"),
      label = "Agriculteur"
    )
    when(mrsValideeCandidat.dateEvaluation) thenReturn LocalDate.now()

    mapping = new MailjetWSMapping(testeurs = Nil)
  }

  "buildContactRequestInscriptionCandidat" should {
    "construire une requete avec la propriété cv à false" in {
      // When
      val request = mapping.buildContactRequestInscriptionCandidat(candidatInscrit)

      // Then
      (request.properties \ cv).as[Boolean] mustBe false
    }
  }
  "buildRequestInscriptionCandidat" should {
    "mettre le candidat dans la liste des testeurs lorsque c'est un testeur" in {
      // Given
      val emailTesteur = Email("candidat.testeur@domain.com")
      when(candidatInscrit.email) thenReturn emailTesteur
      mapping = new MailjetWSMapping(testeurs = List(emailTesteur))

      // When
      val request = mapping.buildRequestInscriptionCandidat(candidatInscrit)

      // Then
      request.idListe mustBe mapping.idListeTesteurs
    }
    "mettre le candidat dans la liste des candidats inscrits lorsque ce n'est pas un testeur" in {
      // When
      val request = mapping.buildRequestInscriptionCandidat(candidatInscrit)

      // Then
      request.idListe mustBe mapping.idListeCandidatsInscrits
    }
  }
  "buildRequestInscriptionRecruteur" should {
    "mettre le recruteur dans la liste des testeurs lorsque c'est un testeur" in {
      // Given
      val emailTesteur = Email("recruteur.testeur@domain.com")
      when(recruteurInscrit.email) thenReturn emailTesteur
      mapping = new MailjetWSMapping(testeurs = List(emailTesteur))

      // When
      val request = mapping.buildRequestInscriptionRecruteur(recruteurInscrit)

      // Then
      request.idListe mustBe mapping.idListeTesteurs
    }
    "mettre le recruteur dans la liste des recruteurs inscrits lorsque ce n'est pas un testeur" in {
      // When
      val request = mapping.buildRequestInscriptionRecruteur(recruteurInscrit)

      // Then
      request.idListe mustBe mapping.idListeRecruteursInscrits
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
}
