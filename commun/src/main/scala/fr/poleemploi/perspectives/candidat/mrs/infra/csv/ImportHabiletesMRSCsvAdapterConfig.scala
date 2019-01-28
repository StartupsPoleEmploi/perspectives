package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.nio.file.Path

import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapterConfig

case class ImportHabiletesMRSCsvAdapterConfig(importDirectory: Path,
                                              archiveDirectory: Path) extends ImportFileAdapterConfig
