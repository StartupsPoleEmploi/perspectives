package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.{MRSValidee, ReferentielHabiletesMRS}
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
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
    dateEvaluation = LocalDate.now(),
    isDHAE = false
  )

  val commande: AjouterMRSValideesCommand =
    AjouterMRSValideesCommand(
      id = candidatBuilder.candidatId,
      mrsValidees = List(mrsValidee)
    )

  var referentielHabiletesMRS: ReferentielHabiletesMRS = _

  before {
    referentielHabiletesMRS = mock[ReferentielHabiletesMRS]
    when(referentielHabiletesMRS.habiletes(ArgumentMatchers.any[CodeROME]())) thenReturn Future.successful(Set.empty[Habilete])
  }

  "ajouterMRSValidee" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When & Then
      recoverToExceptionIf[IllegalStateException](
        candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)
      ).map(ex =>
        ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
      )
    }
    "renvoyer une erreur lorsque le candidat a déjà validé le même métier dans un département" in {
      // Given
      val candidat = candidatBuilder.avecInscription().avecMRSValidee(mrsValidee).build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException](
        candidat.ajouterMRSValidee(commande.copy(
          mrsValidees = List(mrsValidee.copy(dateEvaluation = mrsValidee.dateEvaluation.minusDays(2L)))
        ), referentielHabiletesMRS)
      ).map(ex =>
        ex.getMessage must startWith(s"Le candidat ${candidat.id.value} a déjà validé les métiers suivants")
      )
    }
    "renvoyer une erreur lorsque la commande contient deux fois la même MRS" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException](
        candidat.ajouterMRSValidee(commande.copy(
          mrsValidees = List(mrsValidee, mrsValidee)
        ), referentielHabiletesMRS)
      ).map(ex =>
        ex.getMessage mustBe s"Impossible d'ajouter des MRS au candidat ${candidat.id.value} : la commande contient des MRS avec le même métier pour le même département"
      )
    }
    "renvoyer une erreur lorsque la commande contient deux MRS avec le même métier et le même département : on souhaite juste savoir quel métier a été validé, peu importe si la même MRS est repassée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException](
        candidat.ajouterMRSValidee(commande.copy(
          mrsValidees = List(
            MRSValidee(
              codeROME = CodeROME("H3203"),
              codeDepartement = CodeDepartement("85"),
              dateEvaluation = LocalDate.now(),
              isDHAE = false
            ),
            MRSValidee(
              codeROME = CodeROME("H3203"),
              codeDepartement = CodeDepartement("85"),
              dateEvaluation = LocalDate.now().plusDays(1L),
              isDHAE = false
            )
          )
        ), referentielHabiletesMRS)
      ).map(ex =>
        ex.getMessage mustBe s"Impossible d'ajouter des MRS au candidat ${candidat.id.value} : la commande contient des MRS avec le même métier pour le même département"
      )
    }
    "renvoyer une erreur lorsque la commande contient deux MRS (dont une DHAE) avec le même métier et le même département : on souhaite juste savoir quel métier a été validé, peu importe si la même MRS est repassée" in {
      val candidat = candidatBuilder.avecInscription().build

      // When & Then
      recoverToExceptionIf[IllegalArgumentException](
        candidat.ajouterMRSValidee(commande.copy(
          mrsValidees = List(
            MRSValidee(
              codeROME = CodeROME("H3203"),
              codeDepartement = CodeDepartement("85"),
              dateEvaluation = LocalDate.now(),
              isDHAE = false
            ),
            MRSValidee(
              codeROME = CodeROME("H3203"),
              codeDepartement = CodeDepartement("85"),
              dateEvaluation = LocalDate.now().plusDays(1L),
              isDHAE = true
            )
          )
        ), referentielHabiletesMRS)
      ).map(ex =>
        ex.getMessage mustBe s"Impossible d'ajouter des MRS au candidat ${candidat.id.value} : la commande contient des MRS avec le même métier pour le même département"
      )
    }
    "renvoyer une erreur lorsque le service d'habiletes echoue" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      when(referentielHabiletesMRS.habiletes(mrsValidee.codeROME)) thenReturn Future.failed(new RuntimeException("erreur de service"))

      // When & Then
      recoverToExceptionIf[RuntimeException](
        candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)
      ).map(ex =>
        ex.getMessage mustBe "erreur de service"
      )
    }
    "ne pas générer d'événement lorsqu'aucune MRS n'est à ajouter" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val future = candidat.ajouterMRSValidee(
        command = commande.copy(
          mrsValidees = Nil
        ), referentielHabiletesMRS
      )

      // Then
      future map (_.isEmpty mustBe true)
    }
    "générer un événement lorsque la MRS est ajoutée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val future = candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)

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
          dateEvaluation = LocalDate.now(),
          isDHAE = false
        ), MRSValidee(
          codeROME = CodeROME("2"),
          codeDepartement = CodeDepartement("85"),
          dateEvaluation = LocalDate.now(),
          isDHAE = false
        ))
      )

      // When
      val future = candidat.ajouterMRSValidee(commandeMultiMRS, referentielHabiletesMRS)

      // Then
      future map (events => events.size mustBe commandeMultiMRS.mrsValidees.size)
    }
    "générer un événement contenant la MRS ajoutée" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build
      val habiletes = Set(Habilete("Maintenir son attention dans la durée"))
      when(referentielHabiletesMRS.habiletes(mrsValidee.codeROME)) thenReturn Future.successful(habiletes)

      // When
      val future = candidat.ajouterMRSValidee(commande, referentielHabiletesMRS)

      // Then
      future map (events => {
        val event = events.head.asInstanceOf[MRSAjouteeEvent]
        val mrsValidee = commande.mrsValidees.head

        event.candidatId mustBe commande.id
        event.codeROME mustBe mrsValidee.codeROME
        event.habiletes mustBe habiletes
        event.departement mustBe mrsValidee.codeDepartement
        event.dateEvaluation mustBe mrsValidee.dateEvaluation
        event.isDHAE mustBe mrsValidee.isDHAE
      })
    }
  }

}
