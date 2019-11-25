name := "perspectives-commun"

libraryDependencies ++= Seq(
  ws,
  cacheApi,
  Dependencies.alpakkaCsv,
  Dependencies.alpakkaElasticsearch,
  Dependencies.commonCompress,
  Dependencies.slickPg,
  Dependencies.jwtPlayJson,
  Dependencies.scalatestplus,
  Dependencies.mockito
)
