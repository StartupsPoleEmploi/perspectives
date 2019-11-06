package fr.poleemploi.perspectives.prospect.infra.csv

import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.commun.infra.csv.CsvGenerator
import fr.poleemploi.perspectives.prospect.domain.ProspectCandidat

class ProspectCandidatCsvGenerator extends CsvGenerator[ProspectCandidat] {

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  override val csvHeader = List(
    "Identifiant local",
    "PEConnectId",
    "Nom",
    "Prenom",
    "Email",
    "Genre",
    "Departement",
    "Code ROME MRS",
    "Metier MRS",
    "Date evaluation MRS"
  )

  override def dtoToCsv(prospectCandidat: ProspectCandidat): Option[List[String]] = Some(List(
    prospectCandidat.identifiantLocal.value,
    prospectCandidat.peConnectId.value,
    prospectCandidat.nom.value,
    prospectCandidat.prenom.value,
    prospectCandidat.email.value,
    prospectCandidat.genre.value,
    prospectCandidat.codeDepartement.value,
    prospectCandidat.metier.codeROME.value,
    prospectCandidat.metier.label,
    dateFormatter.format(prospectCandidat.dateEvaluation)
  ))

}
