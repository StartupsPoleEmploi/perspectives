package fr.poleemploi.perspectives.recruteur.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur._
import fr.poleemploi.perspectives.recruteur.alerte.domain.CriteresAlerte
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
    commentaireService.commenterListeCandidats(
      CommentaireListeCandidats(
        nomRecruteur = context.nom.getOrElse(""),
        prenomRecruteur = context.prenom.getOrElse(""),
        raisonSociale = context.raisonSociale.getOrElse(""),
        contexteRecherche = command.contexteRecherche,
        commentaire = command.commentaire
      )
    ).map(_ => Nil)

  override def creerAlerte(context: RecruteurContext, command: CreerAlerteCommand): List[Event] = {
    if (command.localisation.orElse(command.codeSecteurActivite).orElse(command.codeROME).isEmpty) {
      throw new IllegalArgumentException("Au moins un critère doit être renseigné pour une alerte")
    }
    if (context.alertes.size == 10) {
      throw new IllegalArgumentException(s"Le recruteur ${command.id.value} a atteint le nombre maximum d'alertes")
    }
    val criteresAlerte = CriteresAlerte(
      frequence = command.frequenceAlerte,
      codeROME = command.codeROME,
      codeSecteurActivite = command.codeSecteurActivite,
      localisation = command.localisation
    )
    if (context.alertes.values.toList.contains(criteresAlerte)) {
      throw new IllegalArgumentException(s"Une alerte existe déjà pour le recruteur ${command.id.value} avec les critères suivants : $criteresAlerte")
    }

    List(AlerteRecruteurCreeEvent(
      recruteurId = command.id,
      email = context.email.get,
      typeRecruteur = context.typeRecruteur.get,
      alerteId = command.alerteId,
      frequence = command.frequenceAlerte,
      codeROME = command.codeROME,
      codeSecteurActivite = command.codeSecteurActivite,
      localisation = command.localisation
    ))
  }

  override def supprimerAlerte(context: RecruteurContext, command: SupprimerAlerteCommand): List[Event] = {
    if (context.alertes.get(command.alerteId).isEmpty) {
      throw new IllegalArgumentException(s"L'alerte ${command.alerteId.value} n'existe pas")
    }

    List(AlerteRecruteurSupprimeeEvent(
      recruteurId = command.id,
      alerteId = command.alerteId
    ))
  }
}
