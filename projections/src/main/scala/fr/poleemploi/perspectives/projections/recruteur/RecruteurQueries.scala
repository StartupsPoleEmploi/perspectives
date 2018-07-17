package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.Query

sealed trait RecruteurQuery extends Query

case class GetRecruteurQuery(recruteurId: String) extends RecruteurQuery