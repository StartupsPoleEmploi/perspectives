package fr.poleemploi.perspectives.projections.recruteur.alerte

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte}
import fr.poleemploi.perspectives.recruteur.{RecruteurId, TypeRecruteur}

sealed trait AlerteRecruteurDto {

  def recruteurId: RecruteurId

  def typeRecruteur: TypeRecruteur

  def prenom: String

  def email: Email

  def alerteId: AlerteId

  def frequence: FrequenceAlerte
}

case class AlerteRecruteurSecteurDto(recruteurId: RecruteurId,
                                     typeRecruteur: TypeRecruteur,
                                     prenom: String,
                                     email: Email,
                                     alerteId: AlerteId,
                                     frequence: FrequenceAlerte,
                                     secteurActivite: SecteurActivite,
                                     departement: Option[Departement]) extends AlerteRecruteurDto

case class AlerteRecruteurMetierDto(recruteurId: RecruteurId,
                                    typeRecruteur: TypeRecruteur,
                                    prenom: String,
                                    email: Email,
                                    alerteId: AlerteId,
                                    frequence: FrequenceAlerte,
                                    metier: Metier,
                                    departement: Option[Departement]) extends AlerteRecruteurDto

case class AlerteRecruteurDepartementDto(recruteurId: RecruteurId,
                                         typeRecruteur: TypeRecruteur,
                                         prenom: String,
                                         email: Email,
                                         alerteId: AlerteId,
                                         frequence: FrequenceAlerte,
                                         departement: Departement) extends AlerteRecruteurDto