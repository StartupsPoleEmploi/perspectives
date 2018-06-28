package fr.poleemploi.cqrs.command

import fr.poleemploi.eventsourcing.AggregateId

trait Command {

  def id: AggregateId
}
