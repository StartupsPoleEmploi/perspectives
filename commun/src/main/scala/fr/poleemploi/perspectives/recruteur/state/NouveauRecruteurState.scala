package fr.poleemploi.perspectives.recruteur.state
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur.{InscrireRecruteurCommand, RecruteurContext, RecruteurInscritEvent}

object NouveauRecruteurState extends RecruteurState {

  override def inscrire(context: RecruteurContext, command: InscrireRecruteurCommand): List[Event] =
    List(RecruteurInscritEvent(
      recruteurId = command.id,
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = command.genre
    ))
}
