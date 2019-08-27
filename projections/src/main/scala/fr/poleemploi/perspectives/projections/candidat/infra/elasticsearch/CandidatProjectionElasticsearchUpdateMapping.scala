package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.{ExperienceProfessionnelle, Formation}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

import scala.concurrent.ExecutionContext.Implicits.global

class CandidatProjectionElasticsearchUpdateMapping {

  def buildZoneDocument(coordonnees: Coordonnees, rayonRecherche: Option[RayonRecherche]): ZoneDocument =
    ZoneDocument(
      typeMobilite = rayonRecherche.map(_ => "circle").getOrElse("point"),
      latitude = coordonnees.latitude,
      longitude = coordonnees.longitude,
      radius = rayonRecherche
        .map(r => r.uniteLongueur match {
          case UniteLongueur.KM => s"${r.value * 1.2}km"
          case u@_ => throw new IllegalArgumentException(s"Unite de longueur non gérée : ${u.value}")
        })
    )

  def buildRayonRecherche(document: RayonRechercheDocument): RayonRecherche =
    RayonRecherche(
      value = document.value,
      uniteLongueur = document.uniteLongueur
    )

  def buildRayonRechercheDocument(rayonRecherche: RayonRecherche): RayonRechercheDocument =
    RayonRechercheDocument(
      value = rayonRecherche.value,
      uniteLongueur = rayonRecherche.uniteLongueur
    )

  def buildFormationDocument(formation: Formation): FormationDocument =
    FormationDocument(
      anneeFin = formation.anneeFin,
      intitule = formation.intitule,
      lieu = formation.lieu,
      domaine = formation.domaine,
      niveau = formation.niveau
    )

  def buildExperienceProfessionnelleDocument(experienceProfessionnelle: ExperienceProfessionnelle): ExperienceProfessionnelleDocument =
    ExperienceProfessionnelleDocument(
      dateDebut = experienceProfessionnelle.dateDebut,
      dateFin = experienceProfessionnelle.dateFin,
      enPoste = experienceProfessionnelle.enPoste,
      intitule = experienceProfessionnelle.intitule,
      nomEntreprise = experienceProfessionnelle.nomEntreprise,
      lieu = experienceProfessionnelle.lieu,
      description = experienceProfessionnelle.description
    )
}


