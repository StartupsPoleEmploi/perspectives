package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database
import slick.lifted.{Constraint, PrimaryKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MRSDHAEValideesSqlAdapter(val driver: PostgresDriver,
                                database: Database) {

  import driver.api._

  class MRSDHAEValideeCandidatsTable(tag: Tag) extends Table[MRSDHAEValideePEConnect](tag, "candidats_mrs_dhae_validees") {

    def id = column[Long]("id", O.PrimaryKey)

    def peConnectId = column[PEConnectId]("peconnect_id")

    def codeROME = column[CodeROME]("code_rome")

    def codeDepartement = column[CodeDepartement]("code_departement")

    def dateEvaluation = column[LocalDate]("date_evaluation")

    def pk: PrimaryKey = primaryKey("candidats_mrs_dhae_validees_pk", id)

    def idx = index("candidats_mrs_dhae_validees_peconnect_id_idx", peConnectId)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("candidats_mrs_dhae_validees_unicite_mrs", (peConnectId, codeROME, codeDepartement))).toIterator

    def * = (peConnectId, codeROME, codeDepartement, dateEvaluation) <> (MRSDHAEValideePEConnect.tupled, MRSDHAEValideePEConnect.unapply)
  }

  val mrsDHAEValideesCandidatsTable = TableQuery[MRSDHAEValideeCandidatsTable]

  def ajouter(mrsDHAEValidees: Stream[MRSDHAEValideePEConnect]): Future[Unit] = {
    val bulkInsert: DBIO[Option[Int]] = mrsDHAEValideesCandidatsTable.map(
      m => (m.peConnectId, m.codeROME, m.codeDepartement, m.dateEvaluation)
    ) insertOrUpdateAll mrsDHAEValidees.map(
      m => (m.peConnectId, m.codeROME, m.codeDepartement, m.dateEvaluation)
    )

    database.run(bulkInsert).map(_ => ())
  }
}
