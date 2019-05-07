package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database
import slick.lifted.{Constraint, PrimaryKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MRSValideesSqlAdapter(val driver: PostgresDriver,
                            database: Database) {

  import driver.api._

  class MRSValideeCandidatsTable(tag: Tag) extends Table[MRSValideePEConnect](tag, "candidats_mrs_validees") {

    def id = column[Long]("id", O.PrimaryKey)

    def peConnectId = column[PEConnectId]("peconnect_id")

    def codeROME = column[CodeROME]("code_rome")

    def codeDepartement = column[CodeDepartement]("code_departement")

    def dateEvaluation = column[LocalDate]("date_evaluation")

    def pk: PrimaryKey = primaryKey("candidats_mrs_validees_pk", id)

    def idx = index("candidats_mrs_validees_peconnect_id_idx", peConnectId)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("candidats_mrs_validees_unicite_mrs", (peConnectId, codeROME, codeDepartement))).toIterator

    def * = (peConnectId, codeROME, codeDepartement, dateEvaluation) <> (MRSValideePEConnect.tupled, MRSValideePEConnect.unapply)
  }

  val mrsValideesCandidatsTable = TableQuery[MRSValideeCandidatsTable]

  /**
    * Intègre les MRS provenant de l'extract du SI Pôle Emploi
    * Mets à jour les infos de MRS qu'un candidat a déjà validé (contrainte peconnect_id, code_rome, code_departement) car on ne sait pas ce qui a changé dans l'extract ni pour quelle raison, et cela évite d'intégrer des règles issues du SI de Pôle Emploi ici. <br />
    * Par exemple on peut recevoir un enregistrement en VSL puis un autre en VEM, et on ne sait pas dire si c'est une nouvelle MRS ou une mise à jour du statut.
    */
  def ajouter(mrsValidees: Stream[MRSValideePEConnect]): Future[Unit] = {
    val bulkInsert: DBIO[Option[Int]] = mrsValideesCandidatsTable.map(
      m => (m.peConnectId, m.codeROME, m.codeDepartement, m.dateEvaluation)
    ) insertOrUpdateAll mrsValidees.map(
      m => (m.peConnectId, m.codeROME, m.codeDepartement, m.dateEvaluation)
    )

    database.run(bulkInsert).map(_ => ())
  }
}
