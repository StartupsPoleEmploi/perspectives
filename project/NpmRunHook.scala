import play.sbt.PlayRunHook
import sbt._

import scala.sys.process.Process

object NpmRunHook {
  def apply(base: File): PlayRunHook = {

    val uiDirectory: File = base / "ui"

    object NpmProcess extends PlayRunHook {

      var startProcess: Option[Process] = None

      override def beforeStarted(): Unit = {
        if (!(uiDirectory / "node_modules").exists()) Process("npm install", uiDirectory).!
      }

      override def afterStarted(): Unit = {
        startProcess = Some(Process("npm start", uiDirectory).run)
      }

      override def afterStopped(): Unit = {
        startProcess.foreach(_.destroy())
        startProcess = None
      }
    }

    NpmProcess
  }
}