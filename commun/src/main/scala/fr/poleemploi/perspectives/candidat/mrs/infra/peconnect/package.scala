package fr.poleemploi.perspectives.candidat.mrs.infra

import java.util.regex.Pattern

package object peconnect {

  val idPEConnectPattern: Pattern = Pattern.compile("^\\p{Alnum}{8}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{4}-\\p{Alnum}{12}$")

  val sitePrescripteurPattern: Pattern = Pattern.compile("\\d{5}")
}
