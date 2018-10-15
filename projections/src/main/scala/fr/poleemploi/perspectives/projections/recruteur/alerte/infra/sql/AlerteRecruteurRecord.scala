package fr.poleemploi.perspectives.projections.recruteur.alerte.infra.sql

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

case class AlerteRecruteurRecord(recruteurId: RecruteurId,
                                 prenomRecruteur: String,
                                 typeRecruteur: TypeRecruteur,
                                 emailRecruteur: Email,
                                 alerteId: AlerteId,
                                 frequence: FrequenceAlerte,
                                 codeROME: Option[CodeROME],
                                 codeSecteurActivite: Option[CodeSecteurActivite],
                                 codeDepartement: Option[CodeDepartement])
