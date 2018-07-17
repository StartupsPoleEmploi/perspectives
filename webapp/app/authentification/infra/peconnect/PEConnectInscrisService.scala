package authentification.infra.peconnect

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatPEConnect(candidatId: String,
                             peConnectId: String)

case class RecruteurPEConnect(recruteurId: String,
                              peConnectId: String)

class PEConnectInscrisService(val driver: JdbcProfile,
                              database: Database) {

  import driver.api._

  class CandidatPEConnectTable(tag: Tag) extends Table[CandidatPEConnect](tag, "candidats_peconnect") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[String]("candidat_id")

    def peConnectId = column[String]("peconnect_id")

    def * = (candidatId, peConnectId) <> (CandidatPEConnect.tupled, CandidatPEConnect.unapply)
  }

  val candidatsPEConnectTable = TableQuery[CandidatPEConnectTable]

  class RecruteurPEConnectTable(tag: Tag) extends Table[RecruteurPEConnect](tag, "recruteurs_peconnect") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[String]("recruteur_id")

    def peConnectId = column[String]("peconnect_id")

    def * = (recruteurId, peConnectId) <> (RecruteurPEConnect.tupled, RecruteurPEConnect.unapply)
  }

  val recruteursPEConnectTable = TableQuery[RecruteurPEConnectTable]

  def findCandidat(peConnectId: String): Future[Option[CandidatPEConnect]] = {
    val query = candidatsPEConnectTable.filter(u => u.peConnectId === peConnectId)

    database.run(query.result.headOption)
  }

  def saveCandidat(candidat: CandidatPEConnect): Future[Unit] =
    database
      .run(candidatsPEConnectTable.map(
        c => (c.candidatId, c.peConnectId))
        += (candidat.candidatId, candidat.peConnectId))
      .map(_ => ())

  def findRecruteur(peConnectId: String): Future[Option[RecruteurPEConnect]] = {
    val query = recruteursPEConnectTable.filter(u => u.peConnectId === peConnectId)

    database.run(query.result.headOption)
  }

  def saveRecruteur(recruteur: RecruteurPEConnect): Future[Unit] =
    database
      .run(recruteursPEConnectTable.map(
        r => (r.recruteurId, r.peConnectId))
        += (recruteur.recruteurId, recruteur.peConnectId))
      .map(_ => ())

}