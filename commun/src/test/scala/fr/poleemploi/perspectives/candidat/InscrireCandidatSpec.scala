package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.prospect.domain.{ProspectCandidat, ReferentielProspectCandidat}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

import scala.concurrent.Future

class InscrireCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatBuilder = new CandidatBuilder

  val commande: InscrireCandidatCommand =
    InscrireCandidatCommand(
      id = candidatBuilder.candidatId,
      nom = Nom("nom"),
      prenom = Prenom("prenom"),
      email = Email("email@domain.com"),
      genre = Genre.HOMME
    )

  var referentielProspectCandidat: ReferentielProspectCandidat = _

  before {
    referentielProspectCandidat = mock[ReferentielProspectCandidat]
    when(referentielProspectCandidat.find(any())) thenReturn Future.successful(Some(mockProspectCandidat))
  }

  "inscrire" should {
    "renvoyer une erreur lorsque le candidat est déjà inscrit" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalStateException](
        candidat.inscrire(commande, referentielProspectCandidat)
      ).map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut INSCRIT ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "générer un evenement lorsque le candidat n'est pas encore inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande, referentielProspectCandidat)

      // Then
      future map (events => events.size mustBe 1)
    }
    "générer un événement contenant les informations d'inscription" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val future = candidat.inscrire(commande, referentielProspectCandidat)

      // Then
      future map (events => {
        val event = events.filter(_.isInstanceOf[CandidatInscritEvent]).head.asInstanceOf[CandidatInscritEvent]
        event.candidatId mustBe commande.id
        event.nom mustBe commande.nom
        event.prenom mustBe commande.prenom
        event.email mustBe commande.email
        event.genre mustBe commande.genre
        event.identifiantLocal mustBe Some(mockProspectCandidat.identifiantLocal)
        event.peConnectId mustBe Some(mockProspectCandidat.peConnectId)
      })
    }
  }

  private val mockProspectCandidat: ProspectCandidat = {
    val prospectCandidat = mock[ProspectCandidat]
    when(prospectCandidat.identifiantLocal).thenReturn(IdentifiantLocal("123456789A"))
    when(prospectCandidat.peConnectId).thenReturn(PEConnectId("28d0b75a-b694-4de3-8849-18bfbfebd729"))
    prospectCandidat
  }

}
