package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.{AggregateId, Event}

class CandidatCommandHandler(candidatRepository: CandidatRepository)
  extends CommandHandler {

  def inscrire(command: InscrireCandidatCommand): Unit = {
    execute(command.id, _.inscrire(command))
  }

  def modifierCriteresRecherche(command: ModifierCriteresRechercheCommand): Unit = {
    execute(command.id, _.modifierCriteres(command))
  }

  private def execute(aggregateId: AggregateId, f: Candidat => List[Event]): Unit = {
    val aggregate = candidatRepository.getById(aggregateId)

    val events = f(aggregate)

    candidatRepository.save(aggregate, events)
  }
}
