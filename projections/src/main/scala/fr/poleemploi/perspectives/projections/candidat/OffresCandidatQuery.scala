package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.offre.domain.{CriteresRechercheOffre, Offre}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import play.api.libs.json.{Json, Writes}

case class OffresCandidatQuery(criteresRechercheOffre: CriteresRechercheOffre) extends Query[OffresCandidatQueryResult]

case class OffresCandidatQueryResult(offres: List[Offre]) extends QueryResult

object OffresCandidatQueryResult {

  implicit val writes: Writes[Offre] = Writes { o =>
    Json.obj(
      "id" -> o.id,
      "urlOrigine" -> o.urlOrigine,
      "intitule" -> o.intitule,
      "description" -> o.description,
      "libelleLieuTravail" -> o.libelleLieuTravail,
      "typeContrat" -> o.typeContrat,
      "libelleTypeContrat" -> o.libelleTypeContrat,
      "libelleSalaire" -> o.libelleSalaire,
      "libelleDureeTravail" -> o.libelleDureeTravail,
      "libelleExperience" -> o.libelleExperience,
      "metier" -> o.metier,
      "competences" -> o.competences,
      "nomEntreprise" -> o.nomEntreprise,
      "descriptionEntreprise" -> o.descriptionEntreprise,
      "effectifEntreprise" -> o.effectifEntreprise,
      "dateActualisation" -> o.dateActualisation.toLocalDateTime,
      "nomContact" -> o.nomContact,
      "telephoneContact" -> o.telephoneContact,
      "emailContact" -> o.emailContact,
      "coordonneesContact1" -> o.coordonneesContact1,
      "coordonneesContact2" -> o.coordonneesContact2,
      "coordonneesContact3" -> o.coordonneesContact3,
      "urlPostuler" -> o.urlPostuler
    )
  }

}