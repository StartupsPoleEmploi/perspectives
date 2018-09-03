package fr.poleemploi.perspectives.candidat.cv.domain

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.StringValueObject

case class CVId(value: String) extends StringValueObject

case class CV(id: CVId,
              nomFichier: String,
              typeMedia: TypeMedia,
              data: Array[Byte],
              date: ZonedDateTime)

case class DetailsCV(id: CVId,
                     nomFichier: String,
                     typeMedia: TypeMedia,
                     date: ZonedDateTime)
