package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{Coordonnees, RayonRecherche, UniteLongueur}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class CandidatProjectionElasticsearchMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  var referentielMetier: ReferentielMetier = _

  var mapping: CandidatProjectionElasticsearchMapping = _

  before {
    referentielMetier = mock[ReferentielMetier]

    mapping = new CandidatProjectionElasticsearchMapping(referentielMetier)
  }

  "buildZoneDocument" should {
    "renvoyer une erreur lorsque la zone de recherche comme un cercle possède une unité de longueur non gérée" in {
      // Given
      var coordonnees = mock[Coordonnees]
      var rayonRecherche = mock[RayonRecherche]
      when(rayonRecherche.uniteLongueur) thenReturn UniteLongueur("CM")

      // When
      val ex = intercept[IllegalArgumentException](
        mapping.buildZoneDocument(
          coordonnees = coordonnees,
          rayonRecherche = Some(rayonRecherche)
        )
      )

      // Then
      ex.getMessage mustBe "Unite de longueur non gérée CM"
    }
    "construire la zone de recherche avec la latitude" in {
      // Given
      var coordonnees = mock[Coordonnees]
      when(coordonnees.latitude) thenReturn 1.77878

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = None
      )

      // Then
      result.latitude mustBe 1.77878
    }
    "construire la zone de recherche avec la longitude" in {
      // Given
      var coordonnees = mock[Coordonnees]
      when(coordonnees.longitude) thenReturn 48.475645

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = None
      )

      // Then
      result.longitude mustBe 48.475645
    }
    "construire la zone de recherche comme un point lorsqu'il n'y a pas de rayon de recherche" in {
      // Given
      var coordonnees = mock[Coordonnees]

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = None
      )

      // Then
      result.typeMobilite mustBe "point"
      result.radius mustBe None
    }
    "construire la zone de recherche comme un cercle lorsqu'il y a un rayon de recherche" in {
      // Given
      var coordonnees = mock[Coordonnees]
      var rayonRecherche = mock[RayonRecherche]
      when(rayonRecherche.uniteLongueur) thenReturn UniteLongueur.KM

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = Some(rayonRecherche)
      )

      // Then
      result.typeMobilite mustBe "circle"
    }
    "construire la zone de recherche comme un cercle avec l'unité de longueur lorsqu'il y a un rayon de recherche" in {
      // Given
      var coordonnees = mock[Coordonnees]
      var rayonRecherche = mock[RayonRecherche]
      when(rayonRecherche.uniteLongueur) thenReturn UniteLongueur.KM

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = Some(rayonRecherche)
      )

      // Then
      result.radius.exists(_.endsWith("km"))
    }
    "ajouter une marge lorsque la zone de recherche est un cercle, pour ne pas louper les candidats à quelques kilomètres près" in {
      // Given
      var coordonnees = mock[Coordonnees]
      var rayonRecherche = mock[RayonRecherche]
      when(rayonRecherche.value) thenReturn 10
      when(rayonRecherche.uniteLongueur) thenReturn UniteLongueur.KM

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = Some(rayonRecherche)
      )

      // Then
      result.radius mustBe Some("12.0km")
    }
  }
}
