package fr.poleemploi.perspectives.projections.rechercheCandidat

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.commun.domain.{Departement, SecteurActivite}
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService

class RechercheCandidatQueryHandler(rechercheCandidatService: RechercheCandidatService) extends QueryHandler {

  def secteursProposes: List[SecteurActivite] = rechercheCandidatService.secteursProposes

  def departementsProposes: List[Departement] = rechercheCandidatService.departementsProposes
}
