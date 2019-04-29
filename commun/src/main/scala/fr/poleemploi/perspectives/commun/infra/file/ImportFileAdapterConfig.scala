package fr.poleemploi.perspectives.commun.infra.file

import java.nio.file.Path

case class ImportFileAdapterConfig(importDirectory: Path,
                                   archiveDirectory: Path)
