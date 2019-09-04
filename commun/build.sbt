name := "perspectives-commun"

libraryDependencies ++= Seq(
  ws,
  cacheApi,
  Dependencies.alpakka,
  Dependencies.commonCompress,
  Dependencies.slickPg,
  Dependencies.jwtPlayJson,
  Dependencies.scalatestplus,
  Dependencies.mockito
)
