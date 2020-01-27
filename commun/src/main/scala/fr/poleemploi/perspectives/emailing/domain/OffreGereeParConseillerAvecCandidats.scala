package fr.poleemploi.perspectives.emailing.domain

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.offre.domain.OffreId

case class OffreGereeParConseillerAvecCandidats(override val offreId: OffreId,
                                                override val enseigne: String,
                                                override val emailCorrespondant: Email,
                                                override val codeSafir: CodeSafir,
                                                override val intitule: String,
                                                override val codePostal: CodePostal,
                                                override val coordonnees: Coordonnees,
                                                override val codeROME: CodeROME,
                                                override val lieuTravail: String,
                                                override val datePublication: LocalDate,
                                                override val nbCandidats: Int) extends OffreAvecCandidats
