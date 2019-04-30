name := "perspectives-webapp"

maintainer := "brice.friederich@beta.gouv.fr"

libraryDependencies ++= Seq(
  guice,
  ws,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus
)

PlayKeys.playRunHooks += baseDirectory.map(NpmRunHook.apply).value