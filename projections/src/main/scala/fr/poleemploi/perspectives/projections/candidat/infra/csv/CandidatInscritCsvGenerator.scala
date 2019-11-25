package fr.poleemploi.perspectives.projections.candidat.infra.csv

import fr.poleemploi.perspectives.commun.infra.csv.CsvGenerator
import fr.poleemploi.perspectives.projections.candidat.CandidatPourCsvDto

class CandidatInscritCsvGenerator extends CsvGenerator[CandidatPourCsvDto] {

  override val csvHeader = List(
    "Identifiant local",
    "PEConnectId",
    "Nom",
    "Prenom",
    "Email",
    "Genre",
    "Departement",
    "Code ROME MRS"
  )

  override def dtoToCsv(candidatPourCsvDto: CandidatPourCsvDto): Option[List[String]] =
    candidatPourCsvDto.identifiantLocal.map(identifiantLocal =>
      List(
        identifiantLocal.value,
        candidatPourCsvDto.peConnectId.map(_.value).getOrElse(""),
        candidatPourCsvDto.nom.value,
        candidatPourCsvDto.prenom.value,
        candidatPourCsvDto.email.value,
        candidatPourCsvDto.genre.value,
        candidatPourCsvDto.codeDepartement.value,
        candidatPourCsvDto.metier.codeROME.value
      )
    )
}
