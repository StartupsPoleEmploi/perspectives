package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, MustMatchers}

class ModifierCriteresRechercheSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar {

  val candidatBuilder = new CandidatBuilder

  val localisationRecherche: LocalisationRecherche =
    LocalisationRecherche(
      commune = "Paris",
      codePostal = "75011",
      coordonnees = Coordonnees(
        latitude = 48.5,
        longitude = -1.6
      ),
      rayonRecherche = None
    )

  val commande: ModifierCriteresRechercheCommand =
    ModifierCriteresRechercheCommand(
      id = candidatBuilder.candidatId,
      contactRecruteur = false,
      contactFormation = false,
      localisationRecherche = localisationRecherche,
      numeroTelephone = None,
      codesROMEValidesRecherches = Set.empty,
      codesROMERecherches = Set.empty,
      codesDomaineProfessionnelRecherches = Set.empty,
      tempsTravailRecherche = TempsTravail.TEMPS_PLEIN
    )

  "modifierCriteresRecherche" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[IllegalStateException](
        candidat.modifierCriteresRecherche(commande)
      )

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque le contactRecruteur est renseigné mais pas le numéro de téléphone" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException](
        candidat.modifierCriteresRecherche(commande.copy(
          contactRecruteur = true,
          numeroTelephone = None
        ))
      )

      // Then
      ex.getMessage must endWith("Le numéro de téléphone doit être renseigné lorsque le contactRecruteur est souhaité")
    }
    "renvoyer une erreur lorsque le contactFormation est renseigné mais pas le numéro de téléphone" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException](
        candidat.modifierCriteresRecherche(commande.copy(
          contactFormation = true,
          numeroTelephone = None
        ))
      )

      // Then
      ex.getMessage must endWith("Le numéro de téléphone doit être renseigné lorsque le contactFormation est souhaité")
    }
    "renvoyer une erreur lorsqu'un codeROMEValidesRecherche est renseigné alors que le candidat n'a pas validé de codeROME" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException](
        candidat.modifierCriteresRecherche(commande.copy(
          codesROMEValidesRecherches = Set(CodeROME("A1401"))
        ))
      )

      // Then
      ex.getMessage must endWith("Un codeROME ne fait pas partie des codesROME validés par le candidat")
    }
    "renvoyer une erreur lorsqu'un codeROMEValidesRecherche ne fait pas partie d'un codeROME validé par le candidat" in {
      // Given
      val mrsValidee = mock[MRSValidee]
      when(mrsValidee.codeROME) thenReturn CodeROME("K2204")
      val candidat = candidatBuilder
        .avecInscription()
        .avecMRSValidee(mrsValidee)
        .build

      // When
      val ex = intercept[IllegalArgumentException](
        candidat.modifierCriteresRecherche(commande.copy(
          codesROMEValidesRecherches = Set(CodeROME("A1401"))
        ))
      )

      // Then
      ex.getMessage must endWith("Un codeROME ne fait pas partie des codesROME validés par le candidat")
    }
    "ne pas générer d'événement lorsque rien ne change" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande).build

      // When
      val result = candidat.modifierCriteresRecherche(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement lorsque le candidat est inscrit et que le numéro de téléphone est modifié" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande)
        .avecNumeroTelephone(numeroTelephone = Some(NumeroTelephone("0134767892")))
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0234567890"))
      ))

      // Then
      result.count(_.isInstanceOf[NumeroTelephoneModifieEvent]) mustBe 1
    }
    "générer un événement lorsque contactRecruteur est modifié" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande)
        .avecVisibiliteRecruteur(
          contactRecruteur = Some(false)
        )
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        contactRecruteur = true,
        numeroTelephone = Some(NumeroTelephone("0123456789"))
      ))

      // Then
      result.count(_.isInstanceOf[VisibiliteRecruteurModifieeEvent]) mustBe 1
    }
    "générer un événement lorsque le numero de téléphone est modifié" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0123456789"))
      ))
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0988776655"))
      ))

      // Then
      result.count(_.isInstanceOf[NumeroTelephoneModifieEvent]) mustBe 1
    }
    "générer un événement lorsque contactFormation est modifié" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande)
        .avecVisibiliteRecruteur(
          contactFormation = Some(false)
        )
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        contactFormation = true,
        numeroTelephone = Some(NumeroTelephone("0123456789"))
      ))

      // Then
      result.count(_.isInstanceOf[VisibiliteRecruteurModifieeEvent]) mustBe 1
    }
    "générer un événement lorsque la commune est modifiée" in {
      // Given
      val ancienneLocalisation = localisationRecherche.copy(
        commune = "Challans"
      )
      val nouvelleLocalisation = localisationRecherche.copy(
        commune = "La Roche Sur Yon"
      )
      val candidat = candidatInscritAvecCriteres(commande.copy(
        localisationRecherche = ancienneLocalisation
      )).build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque le code postal est modifié" in {
      // Given
      val ancienneLocalisation = localisationRecherche.copy(
        codePostal = "85300"
      )
      val nouvelleLocalisation = localisationRecherche.copy(
        codePostal = "85000"
      )
      val candidat = candidatInscritAvecCriteres(commande.copy(
        localisationRecherche = ancienneLocalisation
      )).build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque les coordonnées sont modifiées" in {
      // Given
      val ancienneLocalisation = localisationRecherche.copy(
        coordonnees = Coordonnees(latitude = 1.4, longitude = 5.2)
      )
      val nouvelleLocalisation = localisationRecherche.copy(
        coordonnees = Coordonnees(latitude = 1.6, longitude = 48.2)
      )
      val candidat = candidatInscritAvecCriteres(commande.copy(
        localisationRecherche = ancienneLocalisation
      )).build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque le rayonRecherche est modifié" in {
      // Given
      val ancienneLocalisation = localisationRecherche.copy(
        rayonRecherche = Some(RayonRecherche.MAX_30)
      )
      val nouvelleLocalisation = localisationRecherche.copy(
        rayonRecherche = Some(RayonRecherche.MAX_50)
      )
      val candidat = candidatInscritAvecCriteres(commande.copy(
        localisationRecherche = ancienneLocalisation
      )).build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier validé est ajouté" in {
      // Given
      val codeROMEValide = CodeROME("H3203")
      val mrsValidee = mock[MRSValidee]
      when(mrsValidee.codeROME) thenReturn codeROMEValide
      val candidat = candidatInscritAvecCriteres(commande.copy(
        codesROMEValidesRecherches = Set.empty
      ))
        .avecMRSValidee(mrsValidee)
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        codesROMEValidesRecherches = Set(codeROMEValide)
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier validé est retiré" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande.copy(
        codesROMEValidesRecherches = Set(CodeROME("H3203"))
      ))
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        codesROMEValidesRecherches = Set.empty
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier recherché est ajouté" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande.copy(
        codesROMERecherches = Set.empty
      ))
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        codesROMERecherches = Set(CodeROME("H3203"))
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier recherché est retiré" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande)
        .avecCriteresRecherche(
          codesROMERecherches = Set(CodeROME("H3203"))
        )
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        codesROMERecherches = commande.codesROMERecherches - CodeROME("H3203")
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque le temps de travail est modifié" in {
      // Given
      val candidat = candidatInscritAvecCriteres(commande)
        .avecCriteresRecherche(
          tempsTravailRecherche = Some(TempsTravail.TEMPS_PARTIEL)
        )
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        tempsTravailRecherche = TempsTravail.TEMPS_PLEIN
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement contenant les critères" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande)

      // Then
      val event = result.filter(_.isInstanceOf[CriteresRechercheModifiesEvent]).head.asInstanceOf[CriteresRechercheModifiesEvent]
      event.candidatId mustBe commande.id
      event.codesROMEValidesRecherches mustBe commande.codesROMEValidesRecherches
      event.codesROMERecherches mustBe commande.codesROMERecherches
      event.localisationRecherche mustBe commande.localisationRecherche
      event.tempsTravailRecherche mustBe commande.tempsTravailRecherche
    }
    "générer un événement contenant le numero de téléphone" in {
      val candidat = candidatBuilder
        .avecInscription()
        .avecNumeroTelephone(
          numeroTelephone = Some(NumeroTelephone("0123456789"))
        )
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0988776655"))
      ))

      // Then
      val event = result.filter(_.isInstanceOf[NumeroTelephoneModifieEvent]).head.asInstanceOf[NumeroTelephoneModifieEvent]
      event.candidatId mustBe commande.id
      event.numeroTelephone mustBe NumeroTelephone("0988776655")
    }
    "générer un événement contenant la visibilité recruteur" in {
      // Given
      val candidat = candidatBuilder
        .avecInscription()
        .build

      // When
      val result = candidat.modifierCriteresRecherche(commande)

      // Then
      val event = result.filter(_.isInstanceOf[VisibiliteRecruteurModifieeEvent]).head.asInstanceOf[VisibiliteRecruteurModifieeEvent]
      event.candidatId mustBe commande.id
      event.contactRecruteur mustBe commande.contactRecruteur
      event.contactFormation mustBe commande.contactFormation
    }
  }

  private def candidatInscritAvecCriteres(commande: ModifierCriteresRechercheCommand): CandidatBuilder =
    candidatBuilder
      .avecInscription()
      .avecCriteresRecherche(
        codesROMERecherches = commande.codesROMERecherches,
        codesROMEValidesRecherches = commande.codesROMEValidesRecherches,
        localisationRecherche = Some(commande.localisationRecherche),
        tempsTravailRecherche = Some(commande.tempsTravailRecherche)
      )
      .avecVisibiliteRecruteur(
        contactRecruteur = Some(commande.contactRecruteur),
        contactFormation = Some(commande.contactFormation)
      )
      .avecNumeroTelephone(numeroTelephone = commande.numeroTelephone)
}
