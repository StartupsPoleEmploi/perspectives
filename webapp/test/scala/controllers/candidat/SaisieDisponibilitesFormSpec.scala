package controllers.candidat

import org.scalatest.{MustMatchers, WordSpec}

class SaisieDisponibilitesFormSpec extends WordSpec
  with MustMatchers {

  "isDisponibilitePasRenseignee" should {
    "doit renvoyer faux si le candidat est encore en recherche" in {
      // When
      val result = SaisieDisponibilitesForm.isDisponibilitePasRenseignee(
        candidatEnRecherche = "true",
        disponibiliteConnue = None,
        nbMoisProchaineDisponibilite = None
      )

      // Then
      result mustBe false
    }
    "doit renvoyer vrai si le candidat n'est plus en recherche et que la disponibilite est vide" in {
      // When
      val result = SaisieDisponibilitesForm.isDisponibilitePasRenseignee(
        candidatEnRecherche = "false",
        disponibiliteConnue = None,
        nbMoisProchaineDisponibilite = None
      )

      // Then
      result mustBe true
    }
    "doit renvoyer vrai si le candidat n'est plus en recherche et que la disponibilite est connue mais sans preciser de date de dispo" in {
      // When
      val result = SaisieDisponibilitesForm.isDisponibilitePasRenseignee(
        candidatEnRecherche = "false",
        disponibiliteConnue = Some("true"),
        nbMoisProchaineDisponibilite = None
      )

      // Then
      result mustBe true
    }
    "doit renvoyer faux si le candidat n'est plus en recherche et que la disponibilite et la date de dispo sont renseignes" in {
      // When
      val result = SaisieDisponibilitesForm.isDisponibilitePasRenseignee(
        candidatEnRecherche = "false",
        disponibiliteConnue = Some("true"),
        nbMoisProchaineDisponibilite = Some(5)
      )

      // Then
      result mustBe false
    }
    "doit renvoyer faux si le candidat n'est plus en recherche et que la disponibilite est inconnue" in {
      // When
      val result = SaisieDisponibilitesForm.isDisponibilitePasRenseignee(
        candidatEnRecherche = "false",
        disponibiliteConnue = Some("false"),
        nbMoisProchaineDisponibilite = None
      )

      // Then
      result mustBe false
    }
  }

  "isEmploiTrouvePasRenseigne" should {
    "doit renvoyer faux si le candidat est encore en recherche" in {
      // When
      val result = SaisieDisponibilitesForm.isEmploiTrouvePasRenseigne(
        candidatEnRecherche = "true",
        emploiTrouveGracePerspectives = None
      )

      // Then
      result mustBe false
    }
    "doit renvoyer vrai si le candidat n'est plus en recherche et que le champ emploi trouve grace a perspectives est vide" in {
      // When
      val result = SaisieDisponibilitesForm.isEmploiTrouvePasRenseigne(
        candidatEnRecherche = "false",
        emploiTrouveGracePerspectives = None
      )

      // Then
      result mustBe true
    }
    "doit renvoyer faux si le candidat n'est plus en recherche et que le champ emploi trouve grace a perspectives est renseigne" in {
      // When
      val result = SaisieDisponibilitesForm.isEmploiTrouvePasRenseigne(
        candidatEnRecherche = "false",
        emploiTrouveGracePerspectives = Some("true")
      )

      // Then
      result mustBe false
    }
  }
}
