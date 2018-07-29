package fr.poleemploi.perspectives.domain.candidat.cv

import java.time.ZonedDateTime

case class CVId(value: String)

case class CV(id: CVId,
              nomFichier: String,
              typeMedia: String,
              data: Array[Byte],
              date: ZonedDateTime)

case class DetailsCV(id: CVId,
                     nomFichier: String,
                     typeMedia: String,
                     date: ZonedDateTime)
