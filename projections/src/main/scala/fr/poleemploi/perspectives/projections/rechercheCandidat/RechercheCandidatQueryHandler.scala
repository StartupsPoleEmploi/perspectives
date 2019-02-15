package fr.poleemploi.perspectives.projections.rechercheCandidat

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService

class RechercheCandidatQueryHandler(rechercheCandidatService: RechercheCandidatService) {

  def secteursProposes: List[SecteurActivite] = rechercheCandidatService.secteursProposes

  def secteursProposesDtos: List[SecteurActiviteDto] = rechercheCandidatService.secteursProposes.map(s =>
    SecteurActiviteDto(
      code = s.code,
      label = s.label
    ))

  def secteurProposeParCode(code: CodeSecteurActivite): SecteurActivite = rechercheCandidatService.secteurActiviteParCode(code)

  def metierProposeParCode(codeROME: CodeROME): Option[Metier] = rechercheCandidatService.metierProposeParCode(codeROME)
}
