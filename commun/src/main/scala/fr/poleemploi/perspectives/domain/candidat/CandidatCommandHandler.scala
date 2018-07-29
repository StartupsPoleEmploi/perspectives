package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.domain.candidat.cv.CVService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatCommandHandler(candidatRepository: CandidatRepository,
                             cvService: CVService)
  extends CommandHandler {

  def inscrire(command: InscrireCandidatCommand): Future[Unit] =
    execute(command.id, c => Future(c.inscrire(command)))

  // FIXME : Le numéro de téléphone est sur le formulaire des critères de recherche pour l'instant
  def modifierCriteresRechercheEtTelephone(modifierCriteresRechercheCommand: ModifierCriteresRechercheCommand,
                                           modifierNumeroTelephoneCommand: ModifierNumeroTelephoneCommand): Future[Unit] =
    execute(modifierCriteresRechercheCommand.id, candidat => Future(
      candidat.modifierCriteres(modifierCriteresRechercheCommand) ++ candidat.modifierNumeroTelephone(modifierNumeroTelephoneCommand)
    ))

  def modifierProfil(command: ModifierProfilPEConnectCommand): Future[Unit] =
    execute(command.id, c => Future(c.modifierProfilPEConnect(command)))

  def ajouterCV(command: AjouterCVCommand): Future[Unit] =
    execute(command.id, _.ajouterCV(command, cvService))

  def remplacerCV(command: RemplacerCVCommand): Future[Unit] =
    execute(command.id, _.remplacerCV(command, cvService))

  private def execute(candidatId: CandidatId, f: Candidat => Future[List[Event]]): Future[Unit] =
    for {
      candidat <- candidatRepository.getById(candidatId)
      events <- f(candidat)
      _ <- candidatRepository.save(candidat, events)
    } yield ()
}
