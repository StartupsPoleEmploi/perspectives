name := "perspectives-webapp"

libraryDependencies ++= Seq(
  guice,
  ws,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus
)

PlayKeys.playRunHooks += baseDirectory.map(NpmRunHook.apply).value