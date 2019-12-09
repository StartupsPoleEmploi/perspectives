package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.offre.domain.OffreId

case class OffreAvecCoordonneesGereeParConseiller(offreId: OffreId,
                                                  enseigne: String,
                                                  emailCorrespondant: Email,
                                                  intitule: String,
                                                  codePostal: CodePostal,
                                                  coordonnees: Coordonnees,
                                                  codeROME: CodeROME,
                                                  lieuTravail: String,
                                                  datePublication: LocalDate)
