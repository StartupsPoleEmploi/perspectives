package fr.poleemploi.perspectives.candidat

import java.nio.file.Path

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.candidat.cv.domain.CVId
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.conseiller.ConseillerId

case class InscrireCandidatCommand(id: CandidatId,
                                   nom: String,
                                   prenom: String,
                                   email: String,
                                   genre: Genre,
                                   adresse: Adresse,
                                   statutDemandeurEmploi: StatutDemandeurEmploi,
                                   mrsValidees: List[MRSValidee]) extends Command

// FIXME : Le numéro de téléphone est sur le formulaire des critères de recherche pour l'instant
case class ModifierCriteresRechercheCommand(id: CandidatId,
                                            rechercheMetierEvalue: Boolean,
                                            rechercheAutreMetier: Boolean,
                                            metiersRecherches: Set[CodeROME],
                                            etreContacteParOrganismeFormation: Boolean,
                                            etreContacteParAgenceInterim: Boolean,
                                            rayonRecherche: RayonRecherche,
                                            numeroTelephone: NumeroTelephone) extends Command

case class ModifierProfilCommand(id: CandidatId,
                                 nom: String,
                                 prenom: String,
                                 email: String,
                                 genre: Genre,
                                 adresse: Adresse,
                                 statutDemandeurEmploi: StatutDemandeurEmploi) extends Command

case class AjouterCVCommand(id: CandidatId,
                            nomFichier: String,
                            typeMedia: String,
                            path: Path) extends Command

case class RemplacerCVCommand(id: CandidatId,
                              cvId: CVId,
                              nomFichier: String,
                              typeMedia: String,
                              path: Path) extends Command

case class AjouterMRSValideesCommand(id: CandidatId,
                                     mrsValidees: List[MRSValidee]) extends Command

case class DeclarerRepriseEmploiParConseillerCommand(id: CandidatId,
                                                     conseillerId: ConseillerId) extends Command