package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.nio.file.Path

import fr.poleemploi.perspectives.commun.infra.file.ImportFileAdapterConfig

case class ImportMRSPEConnectConfig(importDirectory: Path,
                                    archiveDirectory: Path) extends ImportFileAdapterConfig
