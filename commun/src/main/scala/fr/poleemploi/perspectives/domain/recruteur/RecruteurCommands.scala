package fr.poleemploi.perspectives.domain.recruteur

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.domain.{Genre, NumeroTelephone}

case class InscrireRecruteurCommand(id: RecruteurId,
                                    nom: String,
                                    prenom: String,
                                    email: String,
                                    genre: Genre) extends Command

case class ModifierProfilCommand(id: RecruteurId,
                                 raisonSociale: String,
                                 numeroSiret: NumeroSiret,
                                 typeRecruteur: TypeRecruteur,
                                 numeroTelephone: NumeroTelephone,
                                 contactParCandidats: Boolean) extends Command

case class ModifierProfilPEConnectCommand(id: RecruteurId,
                                          nom: String,
                                          prenom: String,
                                          email: String,
                                          genre: Genre) extends Command