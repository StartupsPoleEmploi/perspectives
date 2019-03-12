package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.projections.metier.MetierDTO

case class CandidatSaisieCriteresRechercheQuery(candidatId: CandidatId) extends Query[CandidatSaisieCriteresRechercheQueryResult]

case class CandidatSaisieCriteresRechercheQueryResult(candidatId: CandidatId,
                                                      contactRecruteur: Option[Boolean],
                                                      contactFormation: Option[Boolean],
                                                      metiersValides: Set[MetierDTO],
                                                      metiersValidesRecherches: Set[CodeROME],
                                                      metiersRecherches: Set[CodeROME],
                                                      domainesProfessionnelsRecherches: Set[CodeDomaineProfessionnel],
                                                      numeroTelephone: Option[NumeroTelephone],
                                                      codePostal: Option[String],
                                                      commune: Option[String],
                                                      latitude: Option[Double],
                                                      longitude: Option[Double],
                                                      codePostalRecherche: Option[String],
                                                      communeRecherche: Option[String],
                                                      rayonRecherche: Option[RayonRecherche],
                                                      latitudeRecherche: Option[Double],
                                                      longitudeRecherche: Option[Double]) extends QueryResult {
  def saisieComplete: Boolean =
    List(contactRecruteur, contactFormation, codePostalRecherche, communeRecherche, rayonRecherche).forall(_.isDefined) &&
      contactRecruteur.forall(_ => numeroTelephone.isDefined) &&
      contactFormation.forall(_ => numeroTelephone.isDefined)
}
