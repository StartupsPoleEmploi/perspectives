import scala.sys.process.Process

val buildUI = taskKey[Unit]("Execute le packaging de l'UI avec Node")

buildUI := {
  val process = Process("npm run build", baseDirectory.value / "ui")
  if (process.! != 0) {
    throw new Exception("Echec du packaging de l'UI")
  }
}

dist := (dist dependsOn buildUI).value