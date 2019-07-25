package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.{CandidatId, LocalisationRecherche, TempsTravail}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.metier.domain.Metier

case class CandidatSaisieCriteresRechercheQuery(candidatId: CandidatId) extends Query[CandidatSaisieCriteresRechercheQueryResult]

case class CandidatSaisieCriteresRechercheQueryResult(candidatId: CandidatId,
                                                      contactRecruteur: Option[Boolean],
                                                      contactFormation: Option[Boolean],
                                                      metiersValides: Set[Metier],
                                                      metiersValidesRecherches: Set[CodeROME],
                                                      metiersRecherches: Set[CodeROME],
                                                      domainesProfessionnelsRecherches: Set[CodeDomaineProfessionnel],
                                                      numeroTelephone: Option[NumeroTelephone],
                                                      codePostal: Option[String],
                                                      commune: Option[String],
                                                      latitude: Option[Double],
                                                      longitude: Option[Double],
                                                      localisationRecherche: Option[LocalisationRecherche],
                                                      tempsTravail: Option[TempsTravail]) extends QueryResult {
  def saisieComplete: Boolean =
    List(contactRecruteur, contactFormation, localisationRecherche, tempsTravail).forall(_.isDefined)
}
