package fr.poleemploi.perspectives.domain.candidat

import java.nio.file.Path

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone}


case class InscrireCandidatCommand(override val id: CandidatId,
                                   nom: String,
                                   prenom: String,
                                   email: String,
                                   genre: Genre,
                                   adresse: Adresse,
                                   statutDemandeurEmploi: StatutDemandeurEmploi) extends Command

// FIXME : Le numéro de téléphone est sur le formulaire des critères de recherche pour l'instant
case class ModifierCriteresRechercheCommand(override val id: CandidatId,
                                            rechercheMetierEvalue: Boolean,
                                            rechercheAutreMetier: Boolean,
                                            metiersRecherches: Set[Metier],
                                            etreContacteParOrganismeFormation: Boolean,
                                            etreContacteParAgenceInterim: Boolean,
                                            rayonRecherche: Int,
                                            numeroTelephone: NumeroTelephone) extends Command

case class ModifierProfilPEConnectCommand(override val id: CandidatId,
                                          nom: String,
                                          prenom: String,
                                          email: String,
                                          genre: Genre,
                                          adresse: Adresse,
                                          statutDemandeurEmploi: StatutDemandeurEmploi) extends Command

case class AjouterCVCommand(override val id: CandidatId,
                            nomFichier: String,
                            typeMedia: String,
                            path: Path) extends Command

case class RemplacerCVCommand(override val id: CandidatId,
                              cvId: CVId,
                              nomFichier: String,
                              typeMedia: String,
                              path: Path) extends Command