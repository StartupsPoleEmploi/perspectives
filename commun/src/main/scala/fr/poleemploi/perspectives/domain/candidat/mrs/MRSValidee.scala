package fr.poleemploi.perspectives.domain.candidat.mrs

import java.time.LocalDate

case class MRSValidee(codeMetier: String, // FIXME: avoir un referentiel des métiers pour pouvoir récupérer le métier associé au code
                      dateEvaluation: LocalDate)
