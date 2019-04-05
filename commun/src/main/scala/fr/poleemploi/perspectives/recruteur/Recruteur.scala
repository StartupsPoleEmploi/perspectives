package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.commentaire.domain.CommentaireService
import fr.poleemploi.perspectives.recruteur.state.{NouveauRecruteurState, RecruteurInscritState, RecruteurProfilCompletState, RecruteurState}

import scala.concurrent.Future

class Recruteur(override val id: RecruteurId,
                override val version: Int,
                events: List[Event]) extends Aggregate {

  override type Id = RecruteurId

  private val state: RecruteurState =
    events.foldLeft[RecruteurState](NouveauRecruteurState)((state, event) => event match {
      case _: RecruteurInscritEvent => RecruteurInscritState
      case _: ProfilModifieEvent => RecruteurProfilCompletState
      case _ => state
    })

  private val context: RecruteurContext =
    events.foldLeft(RecruteurContext())((context, event) => event match {
      case e: RecruteurInscritEvent =>
        context.copy(
          nom = Some(e.nom),
          prenom = Some(e.prenom),
          email = Some(e.email),
          genre = Some(e.genre)
        )
      case e: ProfilModifieEvent =>
        context.copy(
          raisonSociale = Some(e.raisonSociale),
          numeroSiret = Some(e.numeroSiret),
          typeRecruteur = Some(e.typeRecruteur),
          contactParCandidats = Some(e.contactParCandidats),
          numeroTelephone = Some(e.numeroTelephone)
        )
      case e: ProfilGerantModifieEvent =>
        context.copy(
          nom = Some(e.nom),
          prenom = Some(e.prenom),
          email = Some(e.email),
          genre = Some(e.genre)
        )
      case _ => context
    })

  def inscrire(command: InscrireRecruteurCommand): List[Event] =
    state.inscrire(context = context, command = command)

  def modifierProfil(command: ModifierProfilCommand): List[Event] =
    state.modifierProfil(context = context, command = command)

  def connecter(command: ConnecterRecruteurCommand): List[Event] =
    state.connecter(context = context, command = command)

  def commenterListeCandidats(command: CommenterListeCandidatsCommand,
                              commentaireService: CommentaireService): Future[List[Event]] =
    state.commenterListeCandidats(context = context, command = command, commentaireService = commentaireService)
}

private[recruteur] case class RecruteurContext(nom: Option[Nom] = None,
                                               prenom: Option[Prenom] = None,
                                               email: Option[Email] = None,
                                               genre: Option[Genre] = None,
                                               raisonSociale: Option[String] = None,
                                               numeroSiret: Option[NumeroSiret] = None,
                                               typeRecruteur: Option[TypeRecruteur] = None,
                                               contactParCandidats: Option[Boolean] = None,
                                               numeroTelephone: Option[NumeroTelephone] = None)
