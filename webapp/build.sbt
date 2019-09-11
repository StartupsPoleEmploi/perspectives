name := "perspectives-webapp"

maintainer := "brice.friederich@beta.gouv.fr"

libraryDependencies ++= Seq(
  guice,
  ws,
  caffeine,
  Dependencies.jwtPlayJson,
  Dependencies.scalaGuice,
  Dependencies.scalatestplus,
  Dependencies.mockito
)

PlayKeys.playRunHooks += baseDirectory.map(NpmRunHook.apply).value
