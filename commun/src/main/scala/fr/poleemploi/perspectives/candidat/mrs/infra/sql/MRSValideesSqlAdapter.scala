package fr.poleemploi.perspectives.candidat.mrs.infra.sql

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.candidat.mrs.infra.MRSValideeCandidatPEConnect
import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Enregistre les MRS des candidats dans Postgres
  */
class MRSValideesSqlAdapter(val driver: PostgresDriver,
                            database: Database) {

  import driver.api._

  class MRSValideeTable(tag: Tag) extends Table[MRSValideeCandidatPEConnect](tag, "mrs_validees") {

    def id = column[Long]("id", O.PrimaryKey)

    def peConnectId = column[PEConnectId]("peconnect_id")

    def codeROME = column[CodeROME]("code_rome")

    def dateEvaluation = column[LocalDate]("date_evaluation")

    def * = (peConnectId, codeROME, dateEvaluation) <> (MRSValideeCandidatPEConnect.tupled, MRSValideeCandidatPEConnect.unapply)
  }

  val mrsValideesTable = TableQuery[MRSValideeTable]
  val metiersEvaluesQuery = Compiled { peConnectId: Rep[PEConnectId] =>
    mrsValideesTable.filter(m => m.peConnectId === peConnectId)
  }

  def ajouter(mrsValidees: Stream[MRSValideeCandidatPEConnect]): Future[Int] = {
    val bulkInsert: DBIO[Option[Int]] = mrsValideesTable.map(
      m => (m.peConnectId, m.codeROME, m.dateEvaluation)
    ) ++= mrsValidees.map(
      m => (m.peConnectId, m.codeROME, m.dateEvaluation)
    )

    database.run(bulkInsert).map(_.getOrElse(0))
  }

  def metiersEvaluesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]] =
    database.run(metiersEvaluesQuery(peConnectId).result)
      .map(_.toList.map(m =>
        MRSValidee(
          codeROME = m.codeROME,
          dateEvaluation = m.dateEvaluation
        ))
      )

}
