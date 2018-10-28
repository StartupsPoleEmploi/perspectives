package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.CommandHandler
import fr.poleemploi.perspectives.recruteur.alerte.domain.AlerteId

trait RecruteurCommandHandler extends CommandHandler[Recruteur] {

  def newRecruteurId: RecruteurId

  def newAlerteId: AlerteId
}
