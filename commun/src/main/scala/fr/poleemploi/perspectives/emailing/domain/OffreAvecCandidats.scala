package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodePostal, CodeROME, CodeSafir, Coordonnees, Email}
import fr.poleemploi.perspectives.offre.domain.OffreId

trait OffreAvecCandidats {

  def offreId: OffreId

  def enseigne: String

  def emailCorrespondant: Email

  def codeSafir: CodeSafir

  def intitule: String

  def codePostal: CodePostal

  def coordonnees: Coordonnees

  def codeROME: CodeROME

  def lieuTravail: String

  def datePublication: LocalDate

  def nbCandidats: Int

}
