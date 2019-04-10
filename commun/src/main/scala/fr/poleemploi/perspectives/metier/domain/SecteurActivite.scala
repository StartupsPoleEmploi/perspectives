package fr.poleemploi.perspectives.metier.domain

import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite

case class SecteurActivite(code: CodeSecteurActivite,
                           label: String,
                           domainesProfessionnels: List[DomaineProfessionnel],
                           metiers: List[Metier])
