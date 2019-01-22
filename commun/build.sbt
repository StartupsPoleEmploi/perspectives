name := "perspectives-commun"

libraryDependencies ++= Seq(
  ws,
  cacheApi,
  Dependencies.alpakka,
  Dependencies.slickPg,
  Dependencies.scalatestplus,
  Dependencies.mockito
)
