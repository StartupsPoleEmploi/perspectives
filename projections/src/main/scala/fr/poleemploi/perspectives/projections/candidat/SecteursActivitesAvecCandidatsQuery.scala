package fr.poleemploi.perspectives.projections.candidat

import fr.poleemploi.cqrs.projection.{Query, QueryResult}
import fr.poleemploi.perspectives.metier.domain.SecteurActivite
import fr.poleemploi.perspectives.recruteur.TypeRecruteur

case class SecteursActivitesAvecCandidatsQuery(typeRecruteur: TypeRecruteur) extends Query[SecteursActivitesAvecCandidatsQueryResult]

case class SecteursActivitesAvecCandidatsQueryResult(secteursActivites: List[SecteurActivite]) extends QueryResult