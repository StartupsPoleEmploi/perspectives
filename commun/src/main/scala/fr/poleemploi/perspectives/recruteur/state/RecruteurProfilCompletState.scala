package fr.poleemploi.perspectives.recruteur.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.recruteur._

object RecruteurProfilCompletState extends RecruteurState {

  override def connecter(context: RecruteurContext, command: ConnecterRecruteurCommand): List[Event] =
    RecruteurInscritState.connecter(context = context, command = command)

  override def modifierProfil(context: RecruteurContext, command: ModifierProfilCommand): List[Event] =
    RecruteurInscritState.modifierProfil(context = context, command = command)
}
