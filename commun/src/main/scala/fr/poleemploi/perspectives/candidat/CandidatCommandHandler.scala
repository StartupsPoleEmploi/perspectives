package fr.poleemploi.perspectives.candidat

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat.cv.domain.CVService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatCommandHandler(candidatRepository: CandidatRepository,
                             cvService: CVService)
  extends CommandHandler {

  def newCandidatId: CandidatId = candidatRepository.newCandidatId

  def inscrire(command: InscrireCandidatCommand): Future[Unit] =
    execute(command.id, c => Future(c.inscrire(command)))

  def modifierCriteresRecherche(command: ModifierCriteresRechercheCommand): Future[Unit] =
    execute(command.id, c => Future(c.modifierCriteres(command)))

  def modifierProfil(command: ModifierProfilPEConnectCommand): Future[Unit] =
    execute(command.id, c => Future(c.modifierProfilPEConnect(command)))

  def ajouterCV(command: AjouterCVCommand): Future[Unit] =
    execute(command.id, _.ajouterCV(command, cvService))

  def remplacerCV(command: RemplacerCVCommand): Future[Unit] =
    execute(command.id, _.remplacerCV(command, cvService))

  def ajouterMRSValidees(command: AjouterMRSValideesCommand): Future[Unit] =
    execute(command.id, c => Future(c.ajouterMRSValidee(command)))

  def declarerRepriseEmploi(command: DeclarerRepriseEmploiParConseillerCommand): Future[Unit] =
    execute(command.id, c => Future(c.declarerRepriseEmploiParConseiller(command)))

  private def execute(candidatId: CandidatId, f: Candidat => Future[List[Event]]): Future[Unit] =
    for {
      candidat <- candidatRepository.getById(candidatId)
      events <- f(candidat)
      _ <- candidatRepository.save(candidat, events)
    } yield ()
}
