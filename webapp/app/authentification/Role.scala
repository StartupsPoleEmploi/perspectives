package authentification

import scala.collection.immutable.ListMap

case class Role(value: String)

object Role {

  val ADMIN = Role(value = "admin")

  private val values = ListMap(
    ADMIN.value -> ADMIN
  )

  def from(r: String): Option[Role] = values.get(r)
}
