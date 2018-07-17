package fr.poleemploi.perspectives.domain.recruteur

/**
  * Value Object NumeroSiret
  */
case class NumeroSiret(value: String)

/**
  * Factory methods pour construire et valider un NumeroSiret
  */
object NumeroSiret {

  def from(value: String): Option[NumeroSiret] = {
    def toDigits(a: Int): Int = a match {
      case n if n >= 0 && n < 10 => n
      case _ => a / 10 + a % 10
    }

    def doubleSeconds(l: Stream[Int]): Stream[Int] =
      l.zip(Stream.from(1)).map(x => if (x._2 % 2 == 0) x._1 * 2 else x._1)

    if (value.matches("\\d{14}") && (doubleSeconds(value.toStream.reverse.map(_.toString.toInt)).map(toDigits).sum % 10 == 0))
      Some(NumeroSiret(value))
    else
      None
  }

}