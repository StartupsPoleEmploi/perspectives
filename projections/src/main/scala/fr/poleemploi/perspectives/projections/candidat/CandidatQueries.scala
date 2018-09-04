package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

sealed trait CandidatQuery extends Query

case class CriteresRechercheQuery(candidatId: CandidatId) extends CandidatQuery

case class CVCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class CVCandidatPourRecruteurQuery(candidatId: CandidatId,
                                        recruteurId: RecruteurId) extends CandidatQuery

case class RechercherCandidatsParDateInscriptionQuery(typeRecruteur: TypeRecruteur) extends CandidatQuery

case class RechercherCandidatsParSecteurQuery(typeRecruteur: TypeRecruteur,
                                              codeSecteurActivite: CodeSecteurActivite) extends CandidatQuery

case class RechercherCandidatsParMetierQuery(typeRecruteur: TypeRecruteur,
                                             codeROME: CodeROME) extends CandidatQuery