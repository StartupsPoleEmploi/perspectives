package authentification.infra.peconnect

import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.recruteur.RecruteurId
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatPEConnect(candidatId: CandidatId,
                             peConnectId: PEConnectId)

case class RecruteurPEConnect(recruteurId: RecruteurId,
                              peConnectId: PEConnectId)

class PEConnectInscrisService(val driver: PostgresDriver,
                              database: Database) {

  import driver.api._

  class CandidatPEConnectTable(tag: Tag) extends Table[CandidatPEConnect](tag, "candidats_peconnect") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def peConnectId = column[PEConnectId]("peconnect_id")

    def * = (candidatId, peConnectId) <> (CandidatPEConnect.tupled, CandidatPEConnect.unapply)
  }

  val candidatsPEConnectTable = TableQuery[CandidatPEConnectTable]
  val findCandidatQuery = Compiled { peConnectId: Rep[PEConnectId] =>
    candidatsPEConnectTable.filter(c => c.peConnectId === peConnectId)
  }

  class RecruteurPEConnectTable(tag: Tag) extends Table[RecruteurPEConnect](tag, "recruteurs_peconnect") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def peConnectId = column[PEConnectId]("peconnect_id")

    def * = (recruteurId, peConnectId) <> (RecruteurPEConnect.tupled, RecruteurPEConnect.unapply)
  }

  val recruteursPEConnectTable = TableQuery[RecruteurPEConnectTable]
  val findRecruteurQuery = Compiled { peConnectId: Rep[PEConnectId] =>
    recruteursPEConnectTable.filter(r => r.peConnectId === peConnectId)
  }

  def findCandidat(peConnectId: PEConnectId): Future[Option[CandidatPEConnect]] =
    database.run(findCandidatQuery(peConnectId).result.headOption)

  def saveCandidat(candidat: CandidatPEConnect): Future[Unit] =
    database
      .run(candidatsPEConnectTable.map(c => (c.candidatId, c.peConnectId))
        += (candidat.candidatId, candidat.peConnectId))
      .map(_ => ())

  def findRecruteur(peConnectId: PEConnectId): Future[Option[RecruteurPEConnect]] =
    database.run(findRecruteurQuery(peConnectId).result.headOption)

  def saveRecruteur(recruteur: RecruteurPEConnect): Future[Unit] =
    database
      .run(recruteursPEConnectTable.map(r => (r.recruteurId, r.peConnectId))
        += (recruteur.recruteurId, recruteur.peConnectId))
      .map(_ => ())

}
