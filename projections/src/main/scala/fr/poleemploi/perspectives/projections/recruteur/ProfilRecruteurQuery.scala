package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import fr.poleemploi.perspectives.recruteur.{Adresse, NumeroSiret, RecruteurId, TypeRecruteur}

case class ProfilRecruteurQuery(recruteurId: RecruteurId) extends Query[ProfilRecruteurQueryResult]

case class ProfilRecruteurQueryResult(recruteurId: RecruteurId,
                                      typeRecruteur: Option[TypeRecruteur],
                                      raisonSociale: Option[String],
                                      adresse: Option[Adresse],
                                      numeroSiret: Option[NumeroSiret],
                                      numeroTelephone: Option[NumeroTelephone],
                                      contactParCandidats: Option[Boolean]) extends QueryResult {
  val profilComplet: Boolean =
    List(typeRecruteur, raisonSociale, adresse, numeroSiret, numeroTelephone, contactParCandidats).forall(_.isDefined)
}