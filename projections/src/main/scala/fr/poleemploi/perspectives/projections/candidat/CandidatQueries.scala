package fr.poleemploi.perspectives.projections.candidat

import java.time.ZonedDateTime

import fr.poleemploi.cqrs.projection.Query
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

sealed trait CandidatQuery extends Query

case class CriteresRechercheQuery(candidatId: CandidatId) extends CandidatQuery

case class CVCandidatQuery(candidatId: CandidatId) extends CandidatQuery

case class CVCandidatPourRecruteurQuery(candidatId: CandidatId,
                                        recruteurId: RecruteurId) extends CandidatQuery

case class CandidatsPourConseillerQuery(nbCandidatsParPage: Int,
                                        avantDateInscription: ZonedDateTime) extends CandidatQuery

case class RechercherCandidatsParDateInscriptionQuery(typeRecruteur: TypeRecruteur,
                                                      codeDepartement: Option[String]) extends CandidatQuery

case class RechercherCandidatsParSecteurQuery(typeRecruteur: TypeRecruteur,
                                              codeSecteurActivite: CodeSecteurActivite,
                                              codeDepartement: Option[String]) extends CandidatQuery

case class RechercherCandidatsParMetierQuery(typeRecruteur: TypeRecruteur,
                                             codeROME: CodeROME,
                                             codeDepartement: Option[String]) extends CandidatQuery
