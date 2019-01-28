package fr.poleemploi.perspectives.commun.infra.file

import java.nio.file.Path

trait ImportFileAdapterConfig {

  def importDirectory: Path

  def  archiveDirectory: Path
}
