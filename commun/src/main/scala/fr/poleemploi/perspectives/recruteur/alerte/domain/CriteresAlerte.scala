package fr.poleemploi.perspectives.recruteur.alerte.domain

import fr.poleemploi.perspectives.commun.domain.{CodeROME, CodeSecteurActivite, Localisation}

case class CriteresAlerte(frequence: FrequenceAlerte,
                          codeROME: Option[CodeROME],
                          codeSecteurActivite: Option[CodeSecteurActivite],
                          localisation: Option[Localisation])
