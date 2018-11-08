package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.mrs.domain.MRSValidee
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Enregistre les MRS des candidats dans Postgres
  */
class MRSValideesCandidatsSqlAdapter(val driver: PostgresDriver,
                                     database: Database) {

  import driver.api._

  class MRSValideeCandidatsTable(tag: Tag) extends Table[MRSValideeCandidatPEConnect](tag, "candidats_mrs_validees") {

    def id = column[Long]("id", O.PrimaryKey)

    def peConnectId = column[PEConnectId]("peconnect_id")

    def codeROME = column[CodeROME]("code_rome")

    def codeDepartement = column[CodeDepartement]("code_departement")

    def dateEvaluation = column[LocalDate]("date_evaluation")

    def * = (peConnectId, codeROME, codeDepartement, dateEvaluation) <> (MRSValideeCandidatPEConnect.tupled, MRSValideeCandidatPEConnect.unapply)
  }

  val mrsValideesCandidatsTable = TableQuery[MRSValideeCandidatsTable]

  implicit object MRSValideeShape extends CaseClassShape(MRSValideeLifted.tupled, MRSValidee.tupled)

  val mrsValideesParCandidatQuery = Compiled { peConnectId: Rep[PEConnectId] =>
    mrsValideesCandidatsTable
      .filter(_.peConnectId === peConnectId)
      .map(mrs => MRSValideeLifted(
        codeROME = mrs.codeROME,
        codeDepartement = mrs.codeDepartement,
        dateEvaluation = mrs.dateEvaluation
      ))
  }

  def ajouter(mrsValidees: Stream[MRSValideeCandidatPEConnect]): Future[Int] = {
    val bulkInsert: DBIO[Option[Int]] = mrsValideesCandidatsTable.map(
      m => (m.peConnectId, m.codeROME, m.codeDepartement, m.dateEvaluation)
    ) ++= mrsValidees.map(
      m => (m.peConnectId, m.codeROME, m.codeDepartement, m.dateEvaluation)
    )

    database.run(bulkInsert).map(_.getOrElse(0))
  }

  def mrsValideesParCandidat(peConnectId: PEConnectId): Future[List[MRSValidee]] =
    database.run(mrsValideesParCandidatQuery(peConnectId).result).map(_.toList)
}
