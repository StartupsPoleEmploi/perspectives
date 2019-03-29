package fr.poleemploi.perspectives.conseiller

import fr.poleemploi.eventsourcing.StringValueObject

case class RoleConseiller(value: String) extends StringValueObject

object RoleConseiller {

  val ADMIN = RoleConseiller(value = "ADMIN")

  private val values = Map(
    ADMIN.value -> ADMIN
  )

  def from(value: String): Option[RoleConseiller] = values.get(value)
}