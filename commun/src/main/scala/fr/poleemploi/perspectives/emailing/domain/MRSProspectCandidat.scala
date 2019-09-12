package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.metier.domain.Metier

trait MRSProspectCandidat {
  def nom: Nom

  def prenom: Prenom

  def email: Email

  def genre: Genre

  def codeDepartement: CodeDepartement

  def metier: Metier

  def dateEvaluation: LocalDate
}
