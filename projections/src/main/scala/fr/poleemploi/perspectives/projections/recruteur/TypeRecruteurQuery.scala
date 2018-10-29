package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

case class TypeRecruteurQuery(recruteurId: RecruteurId) extends Query[TypeRecruteurQueryResult]

case class TypeRecruteurQueryResult(typeRecruteur: Option[TypeRecruteur]) extends QueryResult
