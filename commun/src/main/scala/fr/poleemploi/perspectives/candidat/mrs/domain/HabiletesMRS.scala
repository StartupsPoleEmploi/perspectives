package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}

case class HabiletesMRS(codeROME: CodeROME,
                        habiletes: List[Habilete])
