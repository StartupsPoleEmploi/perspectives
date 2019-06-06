package fr.poleemploi.perspectives.candidat

import java.time.LocalDate

case class ExperienceProfessionnelle(dateDebut: LocalDate,
                                     dateFin: Option[LocalDate],
                                     enPoste: Boolean,
                                     intitule: String,
                                     nomEntreprise: Option[String],
                                     lieu: Option[String],
                                     description: Option[String])
