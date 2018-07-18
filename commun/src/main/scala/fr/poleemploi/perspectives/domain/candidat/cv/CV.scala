package fr.poleemploi.perspectives.domain.candidat.cv

import java.time.ZonedDateTime

import fr.poleemploi.perspectives.domain.candidat.CandidatId

case class CVId(value: String)

case class CV(id: CVId,
              nomFichier: String,
              typeMedia: String,
              date: ZonedDateTime)

case class FichierCV(id: CVId,
                     nomFichier: String,
                     typeMedia: String,
                     data: Array[Byte])

case class CVCandidat(cvId: CVId, candidatId: CandidatId)
