package fr.poleemploi.perspectives.domain.candidat.mrs.infra

import java.time.LocalDate

import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.mrs.MRSValidee
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Enregistre les MRS des candidats dans Postgres
  */
class MRSValideesPostgreSql(val driver: PostgresDriver,
                            database: Database) {

  import driver.api._

  class MRSValideeTable(tag: Tag) extends Table[MRSValideeCandidatPEConnect](tag, "mrs_validees") {

    def id = column[Long]("id", O.PrimaryKey)

    def peConnectId = column[PEConnectId]("peconnect_id")

    def codeMetier = column[String]("code_metier")

    def dateEvaluation = column[LocalDate]("date_evaluation")

    def * = (peConnectId, codeMetier, dateEvaluation) <> (MRSValideeCandidatPEConnect.tupled, MRSValideeCandidatPEConnect.unapply)
  }

  val mrsValideesTable = TableQuery[MRSValideeTable]

  def ajouter(mrsValidees: Stream[MRSValideeCandidatPEConnect]): Future[Int] = {
    val bulkInsert: DBIO[Option[Int]] = mrsValideesTable.map(
      m => (m.peConnectId, m.codeMetier, m.dateEvaluation)
    ) ++= mrsValidees.map(
      m => (m.peConnectId, m.codeMetier, m.dateEvaluation)
    )

    database.run(bulkInsert).map(_.getOrElse(0))
  }

  def metiersEvaluesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]] = {
    val select = mrsValideesTable.filter(m => m.peConnectId === peConnectId)

    database.run(select.result).map(_.toList.map(m => MRSValidee(
      codeMetier = m.codeMetier,
      dateEvaluation = m.dateEvaluation
    )))
  }

}
