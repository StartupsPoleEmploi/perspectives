package fr.poleemploi.perspectives.projections.rechercheCandidat

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService

class RechercheCandidatQueryHandler(rechercheCandidatService: RechercheCandidatService) extends QueryHandler {

  def secteursProposes: List[SecteurActivite] = rechercheCandidatService.secteursProposes

  def secteurProposeParCode(code: CodeSecteurActivite): SecteurActivite = rechercheCandidatService.secteurActiviteParCode(code)

  def metierProposeParCode(codeROME: CodeROME): Option[Metier] = rechercheCandidatService.metierProposeParCode(codeROME)

  def departementsProposes: List[Departement] = rechercheCandidatService.departementsProposes

  def departementParCode(code: CodeDepartement): Departement = rechercheCandidatService.departementParCode(code)
}
