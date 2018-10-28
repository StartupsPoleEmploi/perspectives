package fr.poleemploi.cqrs.command

import fr.poleemploi.eventsourcing.{Aggregate, AggregateRepository, Event}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CommandHandler[A <: Aggregate] {

  def repository: AggregateRepository[A]

  def configure: PartialFunction[Command[A], A => Future[List[Event]]]

  def handle(command: Command[A]): Future[Unit] =
    if (configure.isDefinedAt(command))
      execute(command.id, configure.apply(command))
    else
      Future.failed(new IllegalArgumentException(s"Aucun handler n'est défini pour exécuter la commande $command"))

  private def execute(aggregateId: A#Id, f: A => Future[List[Event]]): Future[Unit] =
    for {
      candidat <- repository.getById(aggregateId)
      events <- f(candidat)
      _ <- repository.save(candidat, events)
    } yield ()
}
