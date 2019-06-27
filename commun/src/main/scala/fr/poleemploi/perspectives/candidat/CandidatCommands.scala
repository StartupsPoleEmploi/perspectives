package fr.poleemploi.perspectives.candidat

import java.nio.file.Path

import fr.poleemploi.cqrs.command.Command
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.conseiller.ConseillerId

case class InscrireCandidatCommand(id: CandidatId,
                                   nom: Nom,
                                   prenom: Prenom,
                                   email: Email,
                                   genre: Genre) extends Command[Candidat]

case class ConnecterCandidatCommand(id: CandidatId,
                                    nom: Nom,
                                    prenom: Prenom,
                                    email: Email,
                                    genre: Genre) extends Command[Candidat]

case class ModifierProfilCandidatCommand(id: CandidatId,
                                         adresse: Option[Adresse],
                                         statutDemandeurEmploi: Option[StatutDemandeurEmploi],
                                         centresInteret: Option[List[CentreInteret]],
                                         langues: Option[List[Langue]],
                                         permis: Option[List[Permis]],
                                         savoirEtre: Option[List[SavoirEtre]],
                                         savoirFaire: Option[List[SavoirFaire]],
                                         formations: Option[List[Formation]],
                                         experiencesProfessionnelles: Option[List[ExperienceProfessionnelle]]) extends Command[Candidat]

case class ModifierCriteresRechercheCommand(id: CandidatId,
                                            contactRecruteur: Boolean,
                                            contactFormation: Boolean,
                                            numeroTelephone: Option[NumeroTelephone],
                                            localisationRecherche: LocalisationRecherche,
                                            codesROMEValidesRecherches: Set[CodeROME],
                                            codesROMERecherches: Set[CodeROME],
                                            codesDomaineProfessionnelRecherches: Set[CodeDomaineProfessionnel],
                                            tempsTravailRecherche: TempsTravail) extends Command[Candidat]

case class AjouterCVCommand(id: CandidatId,
                            typeMedia: TypeMedia,
                            path: Path) extends Command[Candidat]

case class RemplacerCVCommand(id: CandidatId,
                              cvId: CVId,
                              typeMedia: TypeMedia,
                              path: Path) extends Command[Candidat]

case class AjouterMRSValideesCommand(id: CandidatId,
                                     mrsValidees: List[MRSValidee]) extends Command[Candidat]

case class DeclarerRepriseEmploiParConseillerCommand(id: CandidatId,
                                                     conseillerId: ConseillerId) extends Command[Candidat]