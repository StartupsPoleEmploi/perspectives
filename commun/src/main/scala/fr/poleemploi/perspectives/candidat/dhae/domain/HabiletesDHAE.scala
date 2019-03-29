package fr.poleemploi.perspectives.candidat.dhae.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}

/**
  * DHAE (Des habiletés à l'emploi), est un projet de PôleEmploi afin de valider des candidats sur des CodeROME mais sans
  * employeur derrière comme pour une MRS classique. <br />
  * Les habiletés ne sont pas les mêmes que pour les MRS.
  */
case class HabiletesDHAE(codeROME: CodeROME,
                         codeDepartement: CodeDepartement,
                         habiletes: List[Habilete])
