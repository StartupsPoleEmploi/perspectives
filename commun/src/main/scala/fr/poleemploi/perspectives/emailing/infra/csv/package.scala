package fr.poleemploi.perspectives.emailing.infra

import java.util.regex.Pattern

package object csv {

  val identifiantLocalPattern: Pattern = Pattern.compile("^0\\d{9}\\p{Alnum}{1}$")

  val codeNeptunePattern: Pattern = Pattern.compile("^\\p{Alpha}{4}\\p{Digit}{4}$")
}
