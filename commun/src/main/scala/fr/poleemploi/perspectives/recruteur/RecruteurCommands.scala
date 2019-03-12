package fr.poleemploi.perspectives.recruteur

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte, LocalisationAlerte}
import fr.poleemploi.perspectives.recruteur.commentaire.domain.ContexteRecherche

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

case class CommenterListeCandidatsCommand(id: RecruteurId,
                                          contexteRecherche: ContexteRecherche,
                                          commentaire: String) extends Command[Recruteur]

case class CreerAlerteCommand(id: RecruteurId,
                              alerteId: AlerteId,
                              codeSecteurActivite: Option[CodeSecteurActivite],
                              codeROME: Option[CodeROME],
                              localisation: Option[LocalisationAlerte],
                              frequenceAlerte: FrequenceAlerte) extends Command[Recruteur]

case class SupprimerAlerteCommand(id: RecruteurId,
                                  alerteId: AlerteId) extends Command[Recruteur]