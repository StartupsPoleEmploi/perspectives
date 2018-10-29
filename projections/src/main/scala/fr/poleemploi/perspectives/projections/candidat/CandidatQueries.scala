package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.cv.domain.CV
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

case class CandidatSaisieCriteresRechercheQuery(candidatId: CandidatId) extends Query[CandidatSaisieCriteresRechercheDto]

case class CVCandidatQuery(candidatId: CandidatId) extends Query[CVCandidatQueryResult]

case class CVCandidatQueryResult(cv: CV) extends QueryResult

case class CVCandidatPourRecruteurQuery(candidatId: CandidatId,
                                        recruteurId: RecruteurId) extends Query[CVCandidatPourRecruteurQueryResult]

case class CVCandidatPourRecruteurQueryResult(cv: CV) extends QueryResult

case class CandidatsPourConseillerQuery(nbCandidatsParPage: Int,
                                        nbPagesACharger: Int,
                                        avantDateInscription: ZonedDateTime) extends Query[CandidatsPourConseillerQueryResult]

case class CandidatsPourConseillerQueryResult(candidats: List[CandidatPourConseillerDto],
                                              pages: List[ZonedDateTime],
                                              derniereDateInscription: Option[ZonedDateTime]) extends QueryResult

sealed trait RechercherCandidatsQuery extends Query[ResultatRechercheCandidat]

case class RechercherCandidatsParDepartementQuery(typeRecruteur: TypeRecruteur,
                                                  codeDepartement: CodeDepartement,
                                                  apresDateInscription: Option[ZonedDateTime] = None) extends RechercherCandidatsQuery

case class RechercherCandidatsParSecteurQuery(typeRecruteur: TypeRecruteur,
                                              codeSecteurActivite: CodeSecteurActivite,
                                              codeDepartement: Option[CodeDepartement] = None,
                                              apresDateInscription: Option[ZonedDateTime] = None) extends RechercherCandidatsQuery

case class RechercherCandidatsParMetierQuery(typeRecruteur: TypeRecruteur,
                                             codeROME: CodeROME,
                                             codeDepartement: Option[CodeDepartement] = None,
                                             apresDateInscription: Option[ZonedDateTime] = None) extends RechercherCandidatsQuery