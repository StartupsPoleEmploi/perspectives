package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielHabiletesMRS}
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

import scala.concurrent.Future

class AjouterMRSValideeSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val candidatBuilder = new CandidatBuilder

  val mrsValidee = MRSValidee(
    codeROME = CodeROME("H3203"),
    codeDepartement = CodeDepartement("85"),
    dateEvaluation = LocalDate.now()
  )

  val commande: AjouterMRSValideesCommand =
    AjouterMRSValideesCommand(
      id = candidatBuilder.candidatId,
      mrsValidees = List(mrsValidee)
    )

  var referentielHabiletesMRS: ReferentielHabiletesMRS = _

  before {
    referentielHabiletesMRS = mock[ReferentielHabiletesMRS]

    commande.mrsValidees.foreach(m =>
      when(referentielHabiletesMRS.habiletes(m.codeROME, m.codeDepartement)) thenReturn Future.successful(Nil)
    )
  }

  "ajouterMRSValidee" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException] {
        candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)
      }.map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} dans l'état Nouveau ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "renvoyer une erreur lorsque le candidat a déjà passé la MRS" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecMRSValidee(mrsValidee).build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException] {
        candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)
      }.map(ex =>
        ex.getMessage must startWith(s"Le candidat ${candidat.id.value} a déjà validé les MRS suivantes")
      )
    }
    "renvoyer une erreur lorsque le service d'habiletes echoue" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      when(referentielHabiletesMRS.habiletes(mrsValidee.codeROME, mrsValidee.codeDepartement)) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When & Then
      recoverToExceptionIf[RuntimeException] {
        candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)
      }.map(ex =>
        ex.getMessage mustBe "erreur de service"
      )
    }
    "ne pas générer d'événement lorsqu'aucune MRS n'est à ajouter" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val future = candidat.ajouterMRSValidee(
        command = commande.copy(mrsValidees = Nil),
        referentielHabiletesMRS = referentielHabiletesMRS
      )

      // Then
      future map (_.isEmpty mustBe true)
    }
    "générer un événement lorsque la MRS est ajoutée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val future = candidat.ajouterMRSValidee(
        command = commande,
        referentielHabiletesMRS = referentielHabiletesMRS
      )

      // Then
      future map (events => events.size mustBe 1)
    }
    "générer autant d'événément que le nombre de MRS à ajouter" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      val commandeMultiMRS = commande.copy(
        mrsValidees = List(MRSValidee(
          codeROME = CodeROME("1"),
          codeDepartement = CodeDepartement("85"),
          dateEvaluation = LocalDate.now()
        ), MRSValidee(
          codeROME = CodeROME("2"),
          codeDepartement = CodeDepartement("85"),
          dateEvaluation = LocalDate.now()
        ))
      )
      commandeMultiMRS.mrsValidees.foreach(m =>
        when(referentielHabiletesMRS.habiletes(m.codeROME, m.codeDepartement)) thenReturn Future.successful(Nil)
      )

      // When
      val future = candidat.ajouterMRSValidee(
        command = commandeMultiMRS,
        referentielHabiletesMRS = referentielHabiletesMRS
      )

      // Then
      future map (events => events.size mustBe commandeMultiMRS.mrsValidees.size)
    }
    "générer un événement contenant la MRS ajoutée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      val habiletes = List(Habilete("Maintenir son attention dans la durée"))
      when(referentielHabiletesMRS.habiletes(mrsValidee.codeROME, mrsValidee.codeDepartement)) thenReturn Future.successful(habiletes)

      // When
      val future = candidat.ajouterMRSValidee(
        command = commande,
        referentielHabiletesMRS = referentielHabiletesMRS
      )

      // Then
      future map (events => {
        val event = events.head.asInstanceOf[MRSAjouteeEvent]
        event.candidatId mustBe commande.id
        event.metier mustBe commande.mrsValidees.head.codeROME
        event.habiletes mustBe habiletes
        event.departement mustBe commande.mrsValidees.head.codeDepartement
        event.dateEvaluation mustBe commande.mrsValidees.head.dateEvaluation
      })
    }
  }

}
