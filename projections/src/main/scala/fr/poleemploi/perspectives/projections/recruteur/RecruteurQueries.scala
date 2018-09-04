package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.recruteur.RecruteurId

sealed trait RecruteurQuery extends Query

case class ProfilRecruteurQuery(recruteurId: RecruteurId) extends RecruteurQuery