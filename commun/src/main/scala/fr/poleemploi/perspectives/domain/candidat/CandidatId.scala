package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.eventsourcing.AggregateId

case class CandidatId(override val value: String) extends AggregateId
