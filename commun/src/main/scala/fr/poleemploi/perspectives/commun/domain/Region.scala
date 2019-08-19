package fr.poleemploi.perspectives.commun.domain

import fr.poleemploi.eventsourcing.StringValueObject

case class CodeRegion(value: String) extends StringValueObject

case class Region(code: CodeRegion, label: String)