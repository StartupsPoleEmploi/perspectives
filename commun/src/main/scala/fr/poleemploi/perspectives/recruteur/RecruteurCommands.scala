package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.commun.domain._

case class InscrireRecruteurCommand(id: RecruteurId,
                                    nom: Nom,
                                    prenom: Prenom,
                                    email: Email,
                                    genre: Genre) extends Command[Recruteur]

case class ModifierProfilCommand(id: RecruteurId,
                                 raisonSociale: String,
                                 numeroSiret: NumeroSiret,
                                 typeRecruteur: TypeRecruteur,
                                 numeroTelephone: NumeroTelephone,
                                 contactParCandidats: Boolean) extends Command[Recruteur]

case class ConnecterRecruteurCommand(id: RecruteurId,
                                     nom: Nom,
                                     prenom: Prenom,
                                     email: Email,
                                     genre: Genre) extends Command[Recruteur]