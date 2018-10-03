package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.commun.domain.{Email, Genre, NumeroTelephone}
import fr.poleemploi.perspectives.recruteur.commentaire.domain.ContexteRecherche

case class InscrireRecruteurCommand(id: RecruteurId,
                                    nom: String,
                                    prenom: String,
                                    email: Email,
                                    genre: Genre) extends Command

case class ModifierProfilCommand(id: RecruteurId,
                                 raisonSociale: String,
                                 numeroSiret: NumeroSiret,
                                 typeRecruteur: TypeRecruteur,
                                 numeroTelephone: NumeroTelephone,
                                 contactParCandidats: Boolean) extends Command

case class ConnecterRecruteurCommand(id: RecruteurId,
                                     nom: String,
                                     prenom: String,
                                     email: Email,
                                     genre: Genre) extends Command

case class CommenterListeCandidatsCommand(id: RecruteurId,
                                          contexteRecherche: ContexteRecherche,
                                          commentaire: String) extends Command