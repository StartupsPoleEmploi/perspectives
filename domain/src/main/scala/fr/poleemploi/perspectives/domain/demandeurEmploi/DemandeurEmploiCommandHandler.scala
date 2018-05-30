package fr.poleemploi.perspectives.domain.demandeurEmploi

import fr.poleemploi.cqrs.command.{Command, CommandHandler}
import fr.poleemploi.eventsourcing.Event

class DemandeurEmploiCommandHandler(demandeurEmploiRepository: DemandeurEmploiRepository)
  extends CommandHandler[DemandeurEmploi] {

  def execute(command: Command, f: DemandeurEmploi => List[Event]): Unit = {
    val aggregate = demandeurEmploiRepository.getById(command.id)

    val events = f(aggregate)

    demandeurEmploiRepository.save(aggregate, events)
  }
}
