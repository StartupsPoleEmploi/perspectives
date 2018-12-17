package fr.poleemploi.perspectives.recruteur.state
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur._

object RecruteurInscritState extends RecruteurState {

  override def name: String = "Inscrit"

  override def modifierProfil(context: RecruteurContext, command: ModifierProfilCommand): List[Event] =
    if (!context.raisonSociale.contains(command.raisonSociale) ||
      !context.numeroSiret.contains(command.numeroSiret) ||
      !context.typeRecruteur.contains(command.typeRecruteur) ||
      !context.numeroTelephone.contains(command.numeroTelephone)) {
      List(ProfilModifieEvent(
        recruteurId = command.id,
        raisonSociale = command.raisonSociale,
        numeroSiret = command.numeroSiret,
        typeRecruteur = command.typeRecruteur,
        numeroTelephone = command.numeroTelephone
      ))
    } else Nil

  override def connecter(context: RecruteurContext, command: ConnecterRecruteurCommand): List[Event] = {
    val recruteurConnecteEvent = RecruteurConnecteEvent(command.id)

    if (!context.nom.contains(command.nom) ||
      !context.prenom.contains(command.prenom) ||
      !context.email.contains(command.email) ||
      !context.genre.contains(command.genre)) {
      List(recruteurConnecteEvent, ProfilGerantModifieEvent(
        recruteurId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      ))
    } else List(recruteurConnecteEvent)
  }
}
