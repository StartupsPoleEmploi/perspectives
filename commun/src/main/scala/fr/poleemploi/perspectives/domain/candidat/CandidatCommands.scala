package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.eventsourcing.AggregateId
import fr.poleemploi.perspectives.domain.{Genre, Metier}

case class InscrireCandidatCommand(override val id: AggregateId,
                                   nom: String,
                                   prenom: String,
                                   email: String,
                                   genre: Genre) extends Command

case class ModifierCriteresRechercheCommand(override val id: AggregateId,
                                            rechercheMetierEvalue: Boolean,
                                            rechercheAutreMetier: Boolean,
                                            metiersRecherches: Set[Metier],
                                            etreContacteParOrganismeFormation: Boolean,
                                            etreContacteParAgenceInterim: Boolean,
                                            rayonRecherche: Int) extends Command
