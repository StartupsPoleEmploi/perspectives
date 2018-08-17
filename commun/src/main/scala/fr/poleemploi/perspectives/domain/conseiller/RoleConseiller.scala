package fr.poleemploi.perspectives.domain.conseiller

case class RoleConseiller(value: String)

object RoleConseiller {

  val ADMIN = RoleConseiller(value = "admin")

  private val values = Map(
    ADMIN.value -> ADMIN
  )

  def from(value: String): Option[RoleConseiller] = values.get(value)
}