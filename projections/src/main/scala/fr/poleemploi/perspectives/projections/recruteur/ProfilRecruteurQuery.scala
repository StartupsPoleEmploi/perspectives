package fr.poleemploi.perspectives.projections.recruteur

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.commun.domain.NumeroTelephone
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

case class ProfilRecruteurQuery(recruteurId: RecruteurId) extends Query[ProfilRecruteurQueryResult]

case class ProfilRecruteurQueryResult(recruteurId: RecruteurId,
                                      typeRecruteur: Option[TypeRecruteur],
                                      raisonSociale: Option[String],
                                      numeroSiret: Option[NumeroSiret],
                                      numeroTelephone: Option[NumeroTelephone]) extends QueryResult {
  val profilComplet: Boolean =
    List(typeRecruteur, raisonSociale, numeroSiret, numeroTelephone).forall(_.isDefined)
}