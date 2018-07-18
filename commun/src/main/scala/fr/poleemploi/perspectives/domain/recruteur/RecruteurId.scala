package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.eventsourcing.AggregateId

case class RecruteurId(override val value: String) extends AggregateId
