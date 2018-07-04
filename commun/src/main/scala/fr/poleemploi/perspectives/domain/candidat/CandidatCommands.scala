package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.Metier

case class InscrireCandidatCommand(override val id: AggregateId,
                                   peConnectId: String,
                                   nom: String,
                                   prenom: String,
                                   email: String) extends Command

case class ModifierCriteresRechercheCommand(override val id: AggregateId,
                                            rechercheMetierEvalue: Boolean,
                                            rechercheAutreMetier: Boolean,
                                            metiersRecherches: Set[Metier],
                                            etreContacteParOrganismeFormation: Boolean,
                                            etreContacteParAgenceInterim: Boolean,
                                            rayonRecherche: Int) extends Command
