package fr.poleemploi.perspectives.candidat.mrs.infra

import java.util.regex.Pattern

package object peconnect {

  val idPEConnectPattern: Pattern = Pattern.compile("^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$")

  val sitePrescripteurPattern: Pattern = Pattern.compile("[0-9]{5}")
}
