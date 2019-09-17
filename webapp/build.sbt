name := "perspectives-webapp"

maintainer := "mickael.barroux@beta.gouv.fr"

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
