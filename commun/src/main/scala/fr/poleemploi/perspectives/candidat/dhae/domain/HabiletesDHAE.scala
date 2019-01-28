package fr.poleemploi.perspectives.candidat.dhae.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

case class HabiletesDHAE(codeROME: CodeROME,
                         codeDepartement: CodeDepartement,
                         habiletes: List[Habilete])
