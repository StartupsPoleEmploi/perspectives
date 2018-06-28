package fr.poleemploi.perspectives.domain.conseiller

import scala.collection.immutable.ListMap

case class RoleConseiller(value: String)

object RoleConseiller {

  val ADMIN = RoleConseiller(value = "admin")

  private val values = ListMap(
    ADMIN.value -> ADMIN
  )

  def from(r: String): Option[RoleConseiller] = values.get(r)
}