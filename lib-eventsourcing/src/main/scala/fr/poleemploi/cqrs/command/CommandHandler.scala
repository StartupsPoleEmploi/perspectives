package fr.poleemploi.cqrs.command

import fr.poleemploi.eventsourcing.{Aggregate, Event}

trait CommandHandler[A <: Aggregate] {

  def execute(command: Command, f: A => List[Event])
}

// Decorates CommandHandler by adding authorization
trait AuthorizedCommandHandler[B <: Aggregate] extends CommandHandler[B] {
  parent: CommandHandler[B] =>

  def authorize: Boolean

  override def execute(command: Command, f: B => List[Event]): Unit =
    if (authorize)
      parent.execute(command, f)
    else
      throw new RuntimeException("Non autorisé à exécuter la commande")
}
