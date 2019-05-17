package fr.poleemploi.perspectives.candidat

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class ModifierCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val candidatBuilder = new CandidatBuilder

  var localisationRecherche: LocalisationRecherche = _
  var commande: ModifierCandidatCommand = _

  before {
    localisationRecherche = mock[LocalisationRecherche]
    when(localisationRecherche.rayonRecherche) thenReturn None

    commande = ModifierCandidatCommand(
      id = candidatBuilder.candidatId,
      contactRecruteur = false,
      contactFormation = false,
      localisationRecherche = localisationRecherche,
      numeroTelephone = None,
      codesROMEValidesRecherches = Set.empty,
      codesROMERecherches = Set.empty,
      codesDomaineProfessionnelRecherches = Set.empty
    )
  }

  "modifierCriteres" should {
    "renvoyer une erreur lorsque le candidat n'est pas inscrit" in {
      // Given
      val candidat = candidatBuilder.build

      // When
      val ex = intercept[IllegalStateException](
        candidat.modifierCandidat(commande)
      )

      // Then
      ex.getMessage mustBe s"Le candidat ${candidat.id.value} avec le statut NOUVEAU ne peut pas gérer la commande ${commande.getClass.getSimpleName}"
    }
    "renvoyer une erreur lorsque le contactRecruteur est renseigné mais pas le numéro de téléphone" in {
      // Given
      val candidat = candidatBuilder.avecInscription().build

      // When
      val ex = intercept[IllegalArgumentException](
        candidat.modifierCandidat(commande.copy(
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
        candidat.modifierCandidat(commande.copy(
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
        candidat.modifierCandidat(commande.copy(
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
        candidat.modifierCandidat(commande.copy(
          codesROMEValidesRecherches = Set(CodeROME("A1401"))
        ))
      )

      // Then
      ex.getMessage must endWith("Un codeROME ne fait pas partie des codesROME validés par le candidat")
    }
    "ne pas générer d'événement lorsque rien ne change" in {
      // Given
      val candidat = candidatInscritEtModifie(commande).build

      // When
      val result = candidat.modifierCandidat(commande)

      // Then
      result.isEmpty mustBe true
    }
    "générer un événement lorsque le candidat est inscrit et que le numéro de téléphone est modifié" in {
      // Given
      val candidat = candidatInscritEtModifie(commande)
        .avecNumeroTelephone(numeroTelephone = Some(NumeroTelephone("0134767892")))
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0234567890"))
      ))

      // Then
      result.count(_.isInstanceOf[NumeroTelephoneModifieEvent]) mustBe 1
    }
    "générer un événement lorsque contactRecruteur est modifié" in {
      // Given
      val candidat = candidatInscritEtModifie(commande)
        .avecVisibiliteRecruteur(
          contactRecruteur = Some(false)
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        contactRecruteur = true,
        numeroTelephone = Some(NumeroTelephone("0123456789"))
      ))

      // Then
      result.count(_.isInstanceOf[VisibiliteRecruteurModifieeEvent]) mustBe 1
    }
    "générer un événement lorsque le numero de téléphone est modifié" in {
      // Given
      val candidat = candidatInscritEtModifie(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0123456789"))
      ))
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        numeroTelephone = Some(NumeroTelephone("0988776655"))
      ))

      // Then
      result.count(_.isInstanceOf[NumeroTelephoneModifieEvent]) mustBe 1
    }
    "générer un événement lorsque contactFormation est modifié" in {
      // Given
      val candidat = candidatInscritEtModifie(commande)
        .avecVisibiliteRecruteur(
          contactFormation = Some(false)
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        contactFormation = true,
        numeroTelephone = Some(NumeroTelephone("0123456789"))
      ))

      // Then
      result.count(_.isInstanceOf[VisibiliteRecruteurModifieeEvent]) mustBe 1
    }
    "générer un événement lorsque la commune est modifiée" in {
      // Given
      when(localisationRecherche.commune) thenReturn "Challans"
      val nouvelleLocalisation = mock[LocalisationRecherche]
      when(nouvelleLocalisation.commune) thenReturn "La Roche Sur Yon"
      when(nouvelleLocalisation.rayonRecherche) thenReturn None
      val candidat = candidatInscritEtModifie(commande).build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque le code postal est modifié" in {
      // Given
      when(localisationRecherche.codePostal) thenReturn "85300"
      val nouvelleLocalisation = mock[LocalisationRecherche]
      when(nouvelleLocalisation.codePostal) thenReturn "85000"
      when(nouvelleLocalisation.rayonRecherche) thenReturn None
      val candidat = candidatInscritEtModifie(commande).build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque les coordonnées sont modifiées" in {
      // Given
      when(localisationRecherche.coordonnees) thenReturn Coordonnees(latitude = 1.4, longitude = 5.2)
      val nouvelleLocalisation = mock[LocalisationRecherche]
      when(nouvelleLocalisation.coordonnees) thenReturn Coordonnees(latitude = 1.6, longitude = 48.2)
      when(nouvelleLocalisation.rayonRecherche) thenReturn None
      val candidat = candidatInscritEtModifie(commande).build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        localisationRecherche = nouvelleLocalisation
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsque le rayonRecherche est modifié" in {
      // Given
      when(localisationRecherche.rayonRecherche) thenReturn Some(RayonRecherche.MAX_30)
      val nouvelleLocalisation = mock[LocalisationRecherche]
      when(nouvelleLocalisation.rayonRecherche) thenReturn Some(RayonRecherche.MAX_50)
      val candidat = candidatInscritEtModifie(commande).build

      // When
      val result = candidat.modifierCandidat(commande.copy(
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
      val candidat = candidatInscritEtModifie(commande)
        .avecMRSValidee(mrsValidee)
        .avecCriteresRecherche(
          codesROMEValidesRecherches = Set.empty
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        codesROMEValidesRecherches = Set(codeROMEValide)
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier validé est retiré" in {
      // Given
      val candidat = candidatInscritEtModifie(commande)
        .avecCriteresRecherche(
          codesROMEValidesRecherches = Set(CodeROME("H3203"))
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        codesROMEValidesRecherches = commande.codesROMERecherches - CodeROME("H3203")
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier recherché est ajouté" in {
      // Given
      val candidat = candidatInscritEtModifie(commande)
        .avecCriteresRecherche(
          codesROMERecherches = Set.empty
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        codesROMERecherches = commande.codesROMERecherches + CodeROME("H3203")
      ))

      // Then
      result.count(_.isInstanceOf[CriteresRechercheModifiesEvent]) mustBe 1
    }
    "générer un événement lorsqu'un métier recherché est retiré" in {
      // Given
      val candidat = candidatInscritEtModifie(commande)
        .avecCriteresRecherche(
          codesROMERecherches = Set(CodeROME("H3203"))
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
        codesROMERecherches = commande.codesROMERecherches - CodeROME("H3203")
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
      val result = candidat.modifierCandidat(commande)

      // Then
      val event = result.filter(_.isInstanceOf[CriteresRechercheModifiesEvent]).head.asInstanceOf[CriteresRechercheModifiesEvent]
      event.candidatId mustBe commande.id
      event.codesROMEValidesRecherches mustBe commande.codesROMEValidesRecherches
      event.codesROMERecherches mustBe commande.codesROMERecherches
      event.localisationRecherche mustBe commande.localisationRecherche
    }
    "générer un événement contenant le numero de téléphone" in {
      val candidat = candidatBuilder
        .avecInscription()
        .avecNumeroTelephone(
          numeroTelephone = Some(NumeroTelephone("0123456789"))
        )
        .build

      // When
      val result = candidat.modifierCandidat(commande.copy(
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
      val result = candidat.modifierCandidat(commande)

      // Then
      val event = result.filter(_.isInstanceOf[VisibiliteRecruteurModifieeEvent]).head.asInstanceOf[VisibiliteRecruteurModifieeEvent]
      event.candidatId mustBe commande.id
      event.contactRecruteur mustBe commande.contactRecruteur
      event.contactFormation mustBe commande.contactFormation
    }
  }

  private def candidatInscritEtModifie(commande: ModifierCandidatCommand): CandidatBuilder =
    candidatBuilder
      .avecInscription()
      .avecCriteresRecherche(
        codesROMERecherches = commande.codesROMERecherches,
        codesROMEValidesRecherches = commande.codesROMEValidesRecherches,
        localisationRecherche = Some(commande.localisationRecherche)
      )
      .avecVisibiliteRecruteur(
        contactRecruteur = Some(commande.contactRecruteur),
        contactFormation = Some(commande.contactFormation)
      )
      .avecNumeroTelephone(numeroTelephone = commande.numeroTelephone)
}
