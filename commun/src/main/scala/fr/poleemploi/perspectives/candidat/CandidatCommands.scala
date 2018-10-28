package fr.poleemploi.perspectives.candidat

import java.nio.file.Path

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.conseiller.ConseillerId

case class InscrireCandidatCommand(id: CandidatId,
                                   nom: String,
                                   prenom: String,
                                   email: Email,
                                   genre: Genre,
                                   adresse: Option[Adresse],
                                   statutDemandeurEmploi: Option[StatutDemandeurEmploi]) extends Command[Candidat]

// FIXME : Le numéro de téléphone est sur le formulaire des critères de recherche pour l'instant
case class ModifierCriteresRechercheCommand(id: CandidatId,
                                            rechercheMetierEvalue: Boolean,
                                            rechercheAutreMetier: Boolean,
                                            metiersRecherches: Set[CodeROME],
                                            etreContacteParOrganismeFormation: Boolean,
                                            etreContacteParAgenceInterim: Boolean,
                                            rayonRecherche: RayonRecherche,
                                            numeroTelephone: NumeroTelephone) extends Command[Candidat]

case class ConnecterCandidatCommand(id: CandidatId,
                                    nom: String,
                                    prenom: String,
                                    email: Email,
                                    genre: Genre,
                                    adresse: Option[Adresse],
                                    statutDemandeurEmploi: Option[StatutDemandeurEmploi]) extends Command[Candidat]

case class AjouterCVCommand(id: CandidatId,
                            nomFichier: String,
                            typeMedia: TypeMedia,
                            path: Path) extends Command[Candidat]

case class RemplacerCVCommand(id: CandidatId,
                              cvId: CVId,
                              nomFichier: String,
                              typeMedia: TypeMedia,
                              path: Path) extends Command[Candidat]

case class AjouterMRSValideesCommand(id: CandidatId,
                                     mrsValidees: List[MRSValidee]) extends Command[Candidat]

case class DeclarerRepriseEmploiParConseillerCommand(id: CandidatId,
                                                     conseillerId: ConseillerId) extends Command[Candidat]