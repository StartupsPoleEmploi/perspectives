package fr.poleemploi.perspectives.authentification.infra.sql

import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.recruteur.RecruteurId
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class CandidatPEConnect(candidatId: CandidatId,
                             peConnectId: PEConnectId)

case class RecruteurPEConnect(recruteurId: RecruteurId,
                              peConnectId: PEConnectId)

class PEConnectSqlAdapter(val driver: PostgresDriver,
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
  val getCandidatQuery = Compiled { candidatId: Rep[CandidatId] =>
    candidatsPEConnectTable.filter(c => c.candidatId === candidatId)
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

  def getCandidat(candidatId: CandidatId): Future[CandidatPEConnect] =
    database.run(getCandidatQuery(candidatId).result.head)

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
