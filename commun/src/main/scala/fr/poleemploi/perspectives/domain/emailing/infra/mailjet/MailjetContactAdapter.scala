package fr.poleemploi.perspectives.domain.emailing.infra.mailjet

import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.recruteur.RecruteurId
import fr.poleemploi.perspectives.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetContactAdapter(val driver: PostgresDriver,
                            database: Database) {

  import driver.api._

  class CandidatMailjetTable(tag: Tag) extends Table[CandidatMailjet](tag, "candidats_mailjet") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def mailjetContactId = column[MailjetContactId]("mailjet_id")

    def email = column[String]("email")

    def * = (candidatId, mailjetContactId, email) <> (CandidatMailjet.tupled, CandidatMailjet.unapply)
  }

  val candidatsMailjetTable = TableQuery[CandidatMailjetTable]

  class RecruteurMailjetTable(tag: Tag) extends Table[RecruteurMailjet](tag, "recruteurs_mailjet") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def mailjetContactId = column[MailjetContactId]("mailjet_id")

    def email = column[String]("email")

    def * = (recruteurId, mailjetContactId, email) <> (RecruteurMailjet.tupled, RecruteurMailjet.unapply)
  }

  val recruteursMailjetTable = TableQuery[RecruteurMailjetTable]

  def getCandidat(candidatId: CandidatId): Future[CandidatMailjet] = {
    val query = candidatsMailjetTable.filter(c => c.candidatId === candidatId)

    database.run(query.result.head)
  }

  def saveCandidat(candidat: CandidatMailjet): Future[Unit] =
    database
      .run(candidatsMailjetTable.map(c => (c.candidatId, c.mailjetContactId, c.email))
        += (candidat.candidatId, candidat.mailjetContactId, candidat.email))
      .map(_ => ())

  def findRecruteur(recruteurId: RecruteurId): Future[RecruteurMailjet] = {
    val query = recruteursMailjetTable.filter(r => r.recruteurId === recruteurId)

    database.run(query.result.head)
  }

  def saveRecruteur(recruteur: RecruteurMailjet): Future[Unit] =
    database
      .run(recruteursMailjetTable.map(r => (r.recruteurId, r.mailjetContactId, r.email))
        += (recruteur.recruteurId, recruteur.mailjetContactId, recruteur.email))
      .map(_ => ())
}
