package fr.poleemploi.perspectives.candidat.mrs.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

case class HabiletesMRS(codeROME: CodeROME,
                        codeDepartement: CodeDepartement,
                        habiletes: List[Habilete])
