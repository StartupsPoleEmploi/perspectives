package fr.poleemploi.perspectives.recruteur.state
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur._

object RecruteurInscritState extends RecruteurState {

  override def modifierProfil(context: RecruteurContext, command: ModifierProfilCommand): List[Event] = {
    val profilModifieEvent =
      if (!context.raisonSociale.contains(command.raisonSociale) ||
      !context.numeroSiret.contains(command.numeroSiret) ||
      !context.typeRecruteur.contains(command.typeRecruteur) ||
      !context.contactParCandidats.contains(command.contactParCandidats) ||
      !context.numeroTelephone.contains(command.numeroTelephone)) {
      Some(ProfilModifieEvent(
        recruteurId = command.id,
        raisonSociale = command.raisonSociale,
        numeroSiret = command.numeroSiret,
        typeRecruteur = command.typeRecruteur,
        contactParCandidats = command.contactParCandidats,
        numeroTelephone = command.numeroTelephone
      ))
    } else None

    val adresseModifieeEvent =
      if (!context.adresse.contains(command.adresse)) {
        Some(AdresseRecruteurModifieeEvent(
          recruteurId = command.id,
          adresse = command.adresse
        ))
      } else None

    List(profilModifieEvent, adresseModifieeEvent).flatten
  }

  override def connecter(context: RecruteurContext, command: ConnecterRecruteurCommand): List[Event] = {
    val recruteurConnecteEvent = Some(RecruteurConnecteEvent(command.id))

    val profilGerantModifieEvent =
      if (!context.nom.contains(command.nom) ||
      !context.prenom.contains(command.prenom) ||
      !context.email.contains(command.email) ||
      !context.genre.contains(command.genre)) {
      Some(ProfilGerantModifieEvent(
        recruteurId = command.id,
        nom = command.nom,
        prenom = command.prenom,
        email = command.email,
        genre = command.genre
      ))
    } else None

    List(recruteurConnecteEvent, profilGerantModifieEvent).flatten
  }
}
