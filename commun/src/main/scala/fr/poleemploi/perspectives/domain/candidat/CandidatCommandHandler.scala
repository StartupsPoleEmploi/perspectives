package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatCommandHandler(candidatRepository: CandidatRepository)
  extends CommandHandler {

  def inscrire(command: InscrireCandidatCommand): Future[Unit] =
    execute(command.id, _.inscrire(command))

  // FIXME : Le numéro de téléphone est sur le formulaire des critères de recherche pour l'instant
  def modifierCriteresRechercheEtTelephone(modifierCriteresRechercheCommand: ModifierCriteresRechercheCommand,
                                           modifierNumeroTelephoneCommand: ModifierNumeroTelephoneCommand): Future[Unit] =
    execute(modifierCriteresRechercheCommand.id, candidat => {
      candidat.modifierCriteres(modifierCriteresRechercheCommand) ++ candidat.modifierNumeroTelephone(modifierNumeroTelephoneCommand)
    })

  def modifierProfil(command: ModifierProfilPEConnectCommand): Future[Unit] =
    execute(command.id, _.modifierProfilPEConnect(command))

  private def execute(candidatId: CandidatId, f: Candidat => List[Event]): Future[Unit] =
    candidatRepository.getById(candidatId).map(candidat =>
      candidatRepository.save(candidat, f(candidat))
    )
}
