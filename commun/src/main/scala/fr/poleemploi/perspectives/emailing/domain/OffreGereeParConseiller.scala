package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.offre.domain.OffreId

case class OffreGereeParConseiller(offreId: OffreId,
                                   enseigne: String,
                                   emailCorrespondant: Email,
                                   codeSafir: CodeSafir,
                                   intitule: String,
                                   codePostal: CodePostal,
                                   codeROME: CodeROME,
                                   lieuTravail: String,
                                   datePublication: LocalDate)
