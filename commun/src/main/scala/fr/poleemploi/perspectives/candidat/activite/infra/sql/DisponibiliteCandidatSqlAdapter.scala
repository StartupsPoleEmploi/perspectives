package fr.poleemploi.perspectives.candidat.activite.infra.sql

import java.time.LocalDate

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.activite.infra.DisponibiliteCandidat
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.lifted.{Constraint, PrimaryKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DisponibiliteCandidatSqlAdapter(val driver: PostgresDriver,
                                      database: Database) {

  import driver.api._

  class DisponibiliteCandidatTable(tag: Tag) extends Table[DisponibiliteCandidat](tag, "candidats_disponibilite") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def dateDernierEnvoiMail = column[LocalDate]("date_dernier_envoi_mail")

    def pk: PrimaryKey = primaryKey("candidats_disponibilite_pk", id)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("candidats_disponibilite_candidat_id_key", candidatId)).toIterator

    def * = (candidatId, dateDernierEnvoiMail) <> (DisponibiliteCandidat.tupled, DisponibiliteCandidat.unapply)
  }

  val disponibiliteCandidatTable = TableQuery[DisponibiliteCandidatTable]

  def ajouter(disponibiliteCandidat: Seq[CandidatId]): Future[Unit] = {
    val bulkInsert: DBIO[Option[Int]] = disponibiliteCandidatTable.map(
      m => (m.candidatId, m.dateDernierEnvoiMail)
    ) insertOrUpdateAll disponibiliteCandidat.map(
      m => (m, LocalDate.now())
    )

    database.run(bulkInsert).map(_ => ())
  }

  def streamDisponibilites: Source[DisponibiliteCandidat, NotUsed] = Source.fromPublisher {
    database.stream(
      disponibiliteCandidatTable
        .sortBy(_.id)
        .result
        .transactionally
        .withStatementParameters(
          rsType = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize = 1000
        )
    )
  }

}
