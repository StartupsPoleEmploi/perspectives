package fr.poleemploi.perspectives.domain.candidat.cv

import java.time.ZonedDateTime

import fr.poleemploi.eventsourcing.EntityId

case class CVId(value: String) extends EntityId

case class CV(id: CVId,
              nomFichier: String,
              typeMedia: String,
              data: Array[Byte],
              date: ZonedDateTime)

case class DetailsCV(id: CVId,
                     nomFichier: String,
                     typeMedia: String,
                     date: ZonedDateTime)
