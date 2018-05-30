package fr.poleemploi.perspectives.domain.demandeurEmploi

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.AggregateId

/**
  * A command can be serialized and send over the network to a CommandHandler (application service), which can be load
  * balanced
  */
case class InscrireDemandeurEmploiCommand(override val id: AggregateId,
                                          nom: String,
                                          prenom: String,
                                          email: String) extends Command
