package fr.poleemploi.perspectives.candidat.dhae.infra.csv

import java.nio.file.Path

import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapterConfig

case class ImportHabiletesDHAECsvAdapterConfig(importDirectory: Path,
                                               archiveDirectory: Path) extends ImportFileAdapterConfig