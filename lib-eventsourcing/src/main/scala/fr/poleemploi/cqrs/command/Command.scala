package fr.poleemploi.cqrs.command

import fr.poleemploi.eventsourcing.Aggregate

trait Command[A <: Aggregate] {

  def id: A#Id
}