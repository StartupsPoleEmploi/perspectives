package fr.poleemploi.perspectives.projections.metier

import fr.poleemploi.cqrs.projection.QueryHandler
import fr.poleemploi.perspectives.commun.domain.SecteurActivite
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier

class MetierQueryHandler(referentielMetier: ReferentielMetier) extends QueryHandler {

  def secteursProposesPourRecherche: List[SecteurActivite] = referentielMetier.secteursProposesPourRecherche
}
