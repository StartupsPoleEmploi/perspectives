package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.candidat.cv.domain.CVService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait CandidatState {

  def name: String

  def inscrire(context: CandidatContext, command: InscrireCandidatCommand): List[Event] =
    default(context, command)

  def modifierCriteres(context: CandidatContext, command: ModifierCriteresRechercheCommand): List[Event] =
    default(context, command)

  def connecter(context: CandidatContext, command: ConnecterCandidatCommand): List[Event] =
    default(context, command)

  def ajouterCV(context: CandidatContext, command: AjouterCVCommand, cvService: CVService): Future[List[Event]] =
    Future(default(context, command))

  def remplacerCV(context: CandidatContext, command: RemplacerCVCommand, cvService: CVService): Future[List[Event]] =
    Future(default(context, command))

  def ajouterMRSValidee(context: CandidatContext, command: AjouterMRSValideesCommand): List[Event] =
    default(context, command)

  def declarerRepriseEmploiParConseiller(context: CandidatContext, command: DeclarerRepriseEmploiParConseillerCommand): List[Event] =
    default(context, command)

  private def default(context: CandidatContext, command: Command[Candidat]) =
    throw new IllegalArgumentException(s"Le candidat ${command.id.value} dans l'état $name ne peut pas gérer la commande ${command.getClass.getSimpleName}")
}
