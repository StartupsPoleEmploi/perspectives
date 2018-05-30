package fr.poleemploi.eventsourcing.infra.sql

import com.github.tminglei.slickpg._

trait PostgresDriver extends ExPostgresProfile
  with PgJsonSupport {

  object PostgresAPI extends API
    with JsonImplicits
    with SimpleJsonPlainImplicits

  override val api = PostgresAPI

  override def pgjson = "jsonb"
}

object PostgresDriver extends PostgresDriver
