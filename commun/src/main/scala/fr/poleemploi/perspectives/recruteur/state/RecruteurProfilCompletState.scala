package fr.poleemploi.perspectives.recruteur.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object RecruteurProfilCompletState extends RecruteurState {

  override def name: String = "ProfilComplet"

  override def connecter(context: RecruteurContext, command: ConnecterRecruteurCommand): List[Event] =
    RecruteurInscritState.connecter(context = context, command = command)

  override def modifierProfil(context: RecruteurContext, command: ModifierProfilCommand): List[Event] =
    RecruteurInscritState.modifierProfil(context = context, command = command)

  override def commenterListeCandidats(context: RecruteurContext, command: CommenterListeCandidatsCommand, commentaireService: CommentaireService): Future[List[Event]] =
    (for {
      nom <- context.nom
      prenom <- context.prenom
      raisonSociale <- context.raisonSociale
    } yield {
      commentaireService.commenterListeCandidats(
        CommentaireListeCandidats(
          nomRecruteur = nom,
          prenomRecruteur = prenom,
          raisonSociale = raisonSociale,
          contexteRecherche = command.contexteRecherche,
          commentaire = command.commentaire
        )
      ).map(_ => Nil)
    }).getOrElse(Future.successful(Nil))
}
