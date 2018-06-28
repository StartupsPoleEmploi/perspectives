package fr.poleemploi.perspectives.domain

case class NumeroTelephone(value: String)

object NumeroTelephone {

  def from(value: String): Option[NumeroTelephone] =
    if (value.matches("^[\\+]?[0-9]+$") && value.length > 0 && value.length <= 11) {
      Some(NumeroTelephone(value))
    } else None

}
