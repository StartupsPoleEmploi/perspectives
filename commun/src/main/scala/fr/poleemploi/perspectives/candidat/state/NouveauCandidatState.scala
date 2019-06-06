package fr.poleemploi.perspectives.candidat.state

import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.perspectives.candidat._

object NouveauCandidatState extends CandidatState {

  override def inscrire(context: CandidatContext,
                        command: InscrireCandidatCommand): List[Event] = {
    List(CandidatInscritEvent(
      candidatId = command.id,
      nom = command.nom,
      prenom = command.prenom,
      email = command.email,
      genre = command.genre
    ))
  }
}
