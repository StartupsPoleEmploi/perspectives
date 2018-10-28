package fr.poleemploi.perspectives.candidat

import fr.poleemploi.cqrs.command.CommandHandler

trait CandidatCommandHandler extends CommandHandler[Candidat] {

  def newCandidatId: CandidatId
}
