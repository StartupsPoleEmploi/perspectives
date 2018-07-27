package fr.poleemploi.perspectives.domain.recruteur

import java.util.regex.Pattern

import fr.poleemploi.eventsourcing.StringValueObject

/**
  * Value Object NumeroSiret
  */
case class NumeroSiret(value: String) extends StringValueObject

/**
  * Methodes pour construire et valider un NumeroSiret
  */
object NumeroSiret {

  val patternZero: Pattern = Pattern.compile("0{14}")
  val patternSiret: Pattern = Pattern.compile("\\d{14}")

  def from(value: String): Option[NumeroSiret] = {
    def toDigits(a: Int): Int = a match {
      case n if n >= 0 && n < 10 => n
      case _ => a / 10 + a % 10
    }

    def doubleSeconds(l: Stream[Int]): Stream[Int] =
      l.zip(Stream.from(1)).map(x => if (x._2 % 2 == 0) x._1 * 2 else x._1)

    if (!patternZero.matcher(value).matches() && patternSiret.matcher(value).matches() && (doubleSeconds(value.toStream.reverse.map(_.toString.toInt)).map(toDigits).sum % 10 == 0))
      Some(NumeroSiret(value))
    else
      None
  }

}