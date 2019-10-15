package fr.poleemploi.perspectives.commun.infra.file

import java.io.{BufferedWriter, File, FileWriter}

object FileUtils {

  def createTempFile(content: String, filename: String): File = {
    val tempFile = File.createTempFile(filename, "tmp")
    val bw = new BufferedWriter(new FileWriter(tempFile))
    bw.write(content)
    bw.close()
    tempFile.deleteOnExit()
    tempFile
  }
}
