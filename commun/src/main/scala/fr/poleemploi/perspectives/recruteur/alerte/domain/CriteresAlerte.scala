package fr.poleemploi.perspectives.recruteur.alerte.domain

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, CodeSecteurActivite}

case class CriteresAlerte(frequence: FrequenceAlerte,
                          codeROME: Option[CodeROME],
                          codeSecteurActivite: Option[CodeSecteurActivite],
                          codeDepartement: Option[CodeDepartement])
