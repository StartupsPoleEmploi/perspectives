package fr.poleemploi.perspectives.projections.infra

import com.github.tminglei.slickpg._

trait PostgresDriver extends ExPostgresProfile
  with PgArraySupport {

  object PostgresAPI extends API
    with ArrayImplicits {

    implicit val strListTypeMapper = new SimpleArrayJdbcType[String]("text").to(_.toList)
  }

  override val api = PostgresAPI
}

object PostgresDriver extends PostgresDriver