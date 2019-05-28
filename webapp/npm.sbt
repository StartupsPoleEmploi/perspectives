import scala.sys.process.Process

val uiDirectory = settingKey[File]("The ui base directory")

uiDirectory := baseDirectory.value / "ui"

val npmInstall = taskKey[Unit]("Install les modules nécessaires")

npmInstall := {
  val process = Process("npm install", uiDirectory.value)
  if (process.! != 0) {
    throw new Exception("Echec de npm install")
  }
}

val buildUI = taskKey[Unit]("Execute le packaging de l'UI avec Node")

buildUI := {
  val process = Process("npm run build", uiDirectory.value)
  if (process.! != 0) {
    throw new Exception("Echec du packaging de l'UI")
  }
}

dist := (dist dependsOn (buildUI dependsOn npmInstall)).value