package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatCommandHandler(candidatRepository: CandidatRepository)
  extends CommandHandler {

  def inscrire(command: InscrireCandidatCommand): Future[Unit] =
    execute(command.id, _.inscrire(command))

  def modifierCriteresRecherche(command: ModifierCriteresRechercheCommand): Future[Unit] =
    execute(command.id, _.modifierCriteres(command))

  private def execute(candidatId: CandidatId, f: Candidat => List[Event]): Future[Unit] =
    candidatRepository.getById(candidatId).map(candidat =>
      candidatRepository.save(candidat, f(candidat))
    )
}
