package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{Coordonnees, RayonRecherche, UniteLongueur}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

class CandidatProjectionElasticsearchUpdateMappingSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter {

  val rayonRecherche: RayonRecherche = RayonRecherche.MAX_10
  val coordonnees = Coordonnees(latitude = 1.77878, longitude = 48.475645)

  var mapping: CandidatProjectionElasticsearchUpdateMapping = _

  before {
    mapping = new CandidatProjectionElasticsearchUpdateMapping
  }

  "buildZoneDocument" should {
    "renvoyer une erreur lorsque le rayon de recherche comporte une unité de longueur non gérée" in {
      // Given
      val rayonRecherche = RayonRecherche(0, UniteLongueur("CM"))

      // When
      val ex = intercept[IllegalArgumentException](
        mapping.buildZoneDocument(
          coordonnees = coordonnees,
          rayonRecherche = Some(rayonRecherche)
        )
      )

      // Then
      ex.getMessage mustBe "Unite de longueur non gérée : CM"
    }
    "construire la zone de recherche avec la latitude" in {
      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees.copy(
          latitude = 1.2323
        ),
        rayonRecherche = None
      )

      // Then
      result.latitude mustBe 1.2323
    }
    "construire la zone de recherche avec la longitude" in {
      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees.copy(
          longitude = 48.4343
        ),
        rayonRecherche = None
      )

      // Then
      result.longitude mustBe 48.4343
    }
    "construire la zone de recherche comme un point lorsqu'il n'y a pas de rayon de recherche" in {
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
      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = Some(rayonRecherche)
      )

      // Then
      result.typeMobilite mustBe "circle"
      result.radius.isDefined mustBe true
    }
    "construire la zone de recherche avec un rayon lorsqu'il y a un rayon de recherche" in {
      // Given
      val rayonRecherche = RayonRecherche.MAX_10

      // When
      val result = mapping.buildZoneDocument(
        coordonnees = coordonnees,
        rayonRecherche = Some(rayonRecherche)
      )

      // Then
      result.radius.exists(_.endsWith("km"))
    }
    "ajouter une marge lorsqu'il y a un rayon de recherche, pour ne pas louper les candidats à quelques kilomètres près" in {
      // Given
      val rayonRecherche = RayonRecherche.MAX_10

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
