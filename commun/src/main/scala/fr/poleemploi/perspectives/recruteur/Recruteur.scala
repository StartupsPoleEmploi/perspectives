package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.eventsourcing.{Aggregate, Event}
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, CriteresAlerte}
import fr.poleemploi.perspectives.recruteur.commentaire.domain.{CommentaireListeCandidats, CommentaireService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Recruteur(override val id: RecruteurId,
                override val version: Int,
                events: List[Event]) extends Aggregate {

  override type Id = RecruteurId

  private val state: RecruteurState =
    events.foldLeft(RecruteurState())((s, e) => s.apply(e))

  def inscrire(command: InscrireRecruteurCommand): List[Event] = {
    if (state.estInscrit) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} est déjà inscrit")
    }

    List(RecruteurInscritEvent(
      recruteurId = command.id,
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = command.genre
    ))
  }

  def modifierProfil(command: ModifierProfilCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }

    if (!state.raisonSociale.contains(command.raisonSociale) ||
      !state.numeroSiret.contains(command.numeroSiret) ||
      !state.typeRecruteur.contains(command.typeRecruteur) ||
      !state.contactParCandidats.contains(command.contactParCandidats) ||
      !state.numeroTelephone.contains(command.numeroTelephone)) {
      List(ProfilModifieEvent(
        recruteurId = command.id,
        raisonSociale = command.raisonSociale,
        numeroSiret = command.numeroSiret,
        typeRecruteur = command.typeRecruteur,
        contactParCandidats = command.contactParCandidats,
        numeroTelephone = command.numeroTelephone
      ))
    } else Nil
  }

  def connecter(command: ConnecterRecruteurCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }

    val recruteurConnecteEvent = RecruteurConnecteEvent(command.id)

    if (!state.nom.contains(command.nom) ||
      !state.prenom.contains(command.prenom) ||
      !state.email.contains(command.email) ||
      !state.genre.contains(command.genre)) {
      List(recruteurConnecteEvent, ProfilGerantModifieEvent(
        recruteurId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      ))
    } else List(recruteurConnecteEvent)
  }

  def commenterListeCandidats(command: CommenterListeCandidatsCommand,
                              commentaireService: CommentaireService): Future[List[Event]] = {
    if (!state.estInscrit) {
      return Future.failed(new IllegalArgumentException(s"Le recruteur ${id.value} n'est pas encore inscrit"))
    }
    if (!state.avecProfilComplet) {
      return Future.failed(new IllegalArgumentException(s"Le recruteur ${id.value} n'a pas encore complété son profil"))
    }

    commentaireService.commenterListeCandidats(CommentaireListeCandidats(
      nomRecruteur = state.nom.getOrElse(""),
      prenomRecruteur = state.prenom.getOrElse(""),
      raisonSociale = state.raisonSociale.getOrElse(""),
      contexteRecherche = command.contexteRecherche,
      commentaire = command.commentaire
    )).map(_ => Nil)
  }

  def creerAlerte(command: CreerAlerteCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }
    if (!state.avecProfilComplet) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} n'a pas encore complété son profil")
    }
    if (command.codeDepartement
      .orElse(command.codeSecteurActivite)
      .orElse(command.codeROME).isEmpty) {
      throw new IllegalArgumentException("Au moins un critère doit être renseigné pour une alerte")
    }
    if (state.alertes.size == 10) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} a atteint le nombre maximum d'alertes")
    }
    val criteresAlerte = CriteresAlerte(
      command.frequenceAlerte,
      command.codeROME,
      command.codeSecteurActivite,
      command.codeDepartement
    )
    if (state.alertes.values.toList.contains(criteresAlerte)) {
      throw new IllegalArgumentException(s"Une alerte existe déjà pour le recruteur ${id.value} avec les critères suivants : $criteresAlerte")
    }

    List(AlerteRecruteurCreeEvent(
      recruteurId = command.id,
      prenom = state.prenom.get,
      email = state.email.get,
      typeRecruteur = state.typeRecruteur.get,
      alerteId = command.alerteId,
      frequence = command.frequenceAlerte,
      codeROME = command.codeROME,
      codeSecteurActivite = command.codeSecteurActivite,
      codeDepartement = command.codeDepartement
    ))
  }

  def supprimerAlerte(command: SupprimerAlerteCommand): List[Event] = {
    if (!state.estInscrit) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} n'est pas encore inscrit")
    }
    if (!state.avecProfilComplet) {
      throw new IllegalArgumentException(s"Le recruteur ${id.value} n'a pas encore complété son profil")
    }
    if (state.alertes.get(command.alerteId).isEmpty) {
      throw new IllegalArgumentException(s"L'alerte ${command.alerteId.value} n'existe pas")
    }

    List(AlerteRecruteurSupprimeeEvent(
      recruteurId = command.id,
      alerteId = command.alerteId
    ))
  }
}

private[recruteur] case class RecruteurState(estInscrit: Boolean = false,
                                             avecProfilComplet: Boolean = false,
                                             nom: Option[String] = None,
                                             prenom: Option[String] = None,
                                             email: Option[Email] = None,
                                             genre: Option[Genre] = None,
                                             raisonSociale: Option[String] = None,
                                             numeroSiret: Option[NumeroSiret] = None,
                                             typeRecruteur: Option[TypeRecruteur] = None,
                                             contactParCandidats: Option[Boolean] = None,
                                             numeroTelephone: Option[NumeroTelephone] = None,
                                             alertes: Map[AlerteId, CriteresAlerte] = Map()) {

  def apply(event: Event): RecruteurState = event match {
    case e: RecruteurInscritEvent =>
      copy(
        estInscrit = true,
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre)
      )
    case e: ProfilModifieEvent =>
      copy(
        avecProfilComplet = true,
        raisonSociale = Some(e.raisonSociale),
        numeroSiret = Some(e.numeroSiret),
        typeRecruteur = Some(e.typeRecruteur),
        contactParCandidats = Some(e.contactParCandidats),
        numeroTelephone = Some(e.numeroTelephone)
      )
    case e: ProfilGerantModifieEvent =>
      copy(
        nom = Some(e.nom),
        prenom = Some(e.prenom),
        email = Some(e.email),
        genre = Some(e.genre)
      )
    case e: AlerteRecruteurCreeEvent =>
      copy(
        alertes = alertes + (e.alerteId -> CriteresAlerte(
          frequence = e.frequence,
          codeROME = e.codeROME,
          codeSecteurActivite = e.codeSecteurActivite,
          codeDepartement = e.codeDepartement
        ))
      )
    case e: AlerteRecruteurSupprimeeEvent =>
      copy(
        alertes = alertes - e.alerteId
      )
    case _ => this
  }
}
