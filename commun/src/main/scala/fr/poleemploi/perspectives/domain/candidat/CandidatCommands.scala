package fr.poleemploi.perspectives.domain.candidat

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone}

case class InscrireCandidatCommand(override val id: CandidatId,
                                   nom: String,
                                   prenom: String,
                                   email: String,
                                   genre: Genre) extends Command

case class ModifierCriteresRechercheCommand(override val id: CandidatId,
                                            rechercheMetierEvalue: Boolean,
                                            rechercheAutreMetier: Boolean,
                                            metiersRecherches: Set[Metier],
                                            etreContacteParOrganismeFormation: Boolean,
                                            etreContacteParAgenceInterim: Boolean,
                                            rayonRecherche: Int) extends Command

case class ModifierProfilPEConnectCommand(override val id: CandidatId,
                                          nom: String,
                                          prenom: String,
                                          email: String,
                                          genre: Genre) extends Command

case class ModifierNumeroTelephoneCommand(override val id: CandidatId,
                                          numeroTelephone: NumeroTelephone) extends Command