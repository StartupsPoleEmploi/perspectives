package fr.poleemploi.perspectives.emailing.infra.ws

import fr.poleemploi.perspectives.commun.domain.{Email, Genre, Nom, Prenom}
import fr.poleemploi.perspectives.emailing.domain.{CandidatInscrit, RecruteurInscrit}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class MailjetWSMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  var mapping: MailjetWSMapping = _

  var candidatInscrit: CandidatInscrit = _
  var recruteurInscrit: RecruteurInscrit = _

  before {
    candidatInscrit = mock[CandidatInscrit]
    when(candidatInscrit.nom) thenReturn Nom("Nom")
    when(candidatInscrit.prenom) thenReturn Prenom("Prenom")
    when(candidatInscrit.genre) thenReturn Genre.HOMME

    recruteurInscrit = mock[RecruteurInscrit]
    when(candidatInscrit.nom) thenReturn Nom("Nom")
    when(candidatInscrit.prenom) thenReturn Prenom("Prenom")
    when(candidatInscrit.genre) thenReturn Genre.HOMME

    mapping = new MailjetWSMapping(testeurs = Nil)
  }

  "buildContactRequestInscriptionCandidat" should {
    "construire une requete avec la propriété cv à false" in {
      // When
      val request = mapping.buildContactRequestInscriptionCandidat(candidatInscrit)

      // Then
      request.properties
        .exists(jsValue => (jsValue \ "Name").as[String] == "cv"
          && !(jsValue \ "Value").as[Boolean]) mustBe true
    }
  }
  "buildContactListsRequestInscriptionCandidat" should {
    "mettre le candidat dans la liste des testeurs lorsque c'est un testeur" in {
      // Given
      val emailTesteur = Email("candidat.testeur@domain.com")
      when(candidatInscrit.email) thenReturn emailTesteur
      mapping = new MailjetWSMapping(testeurs = List(emailTesteur))

      // When
      val request = mapping.buildContactListsRequestInscriptionCandidat(candidatInscrit)

      // Then
      request.contactsList
        .exists(contactList => contactList.listID == mapping.idListeTesteurs.toString
          && contactList.action == "addnoforce") mustBe true
    }
    "mettre le candidat dans la liste des candidats inscrits lorsque ce n'est pas un testeur" in {
      // When
      val request = mapping.buildContactListsRequestInscriptionCandidat(candidatInscrit)

      // Then
      request.contactsList
        .exists(contactList => contactList.listID == mapping.idListeCandidatsInscrits.toString
          && contactList.action == "addnoforce") mustBe true
    }
  }
  "buildContactListsRequestInscriptionRecruteur" should {
    "mettre le recruteur dans la liste des testeurs lorsque c'est un testeur" in {
      // Given
      val emailTesteur = Email("recruteur.testeur@domain.com")
      when(recruteurInscrit.email) thenReturn emailTesteur
      mapping = new MailjetWSMapping(testeurs = List(emailTesteur))

      // When
      val request = mapping.buildContactListsRequestInscriptionRecruteur(recruteurInscrit)

      // Then
      request.contactsList
        .exists(contactList => contactList.listID == mapping.idListeTesteurs.toString
          && contactList.action == "addnoforce") mustBe true
    }
    "mettre le recruteur dans la liste des recruteurs inscrits lorsque ce n'est pas un testeur" in {
      // When
      val request = mapping.buildContactListsRequestInscriptionRecruteur(recruteurInscrit)

      // Then
      request.contactsList
        .exists(contactList => contactList.listID == mapping.idListeRecruteursInscrits.toString
          && contactList.action == "addnoforce") mustBe true
    }
  }
}
