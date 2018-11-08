package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.nio.file.Path

case class ImportHabiletesMRSCsvAdapterConfig(importDirectory: Path,
                                              archiveDirectory: Path)
