package fr.poleemploi.perspectives.emailing.infra.sql

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.emailing.infra.mailjet.{CandidatMailjet, MailjetContactId, RecruteurMailjet}
import fr.poleemploi.perspectives.recruteur.RecruteurId
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{ResultSetConcurrency, ResultSetType}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetSqlAdapter(val driver: PostgresDriver,
                        database: Database) {

  import driver.api._

  class CandidatMailjetTable(tag: Tag) extends Table[CandidatMailjet](tag, "candidats_mailjet") {

    def id = column[Long]("id", O.PrimaryKey)

    def candidatId = column[CandidatId]("candidat_id")

    def mailjetContactId = column[MailjetContactId]("mailjet_id")

    def email = column[Email]("email")

    def * = (candidatId, mailjetContactId, email) <> (CandidatMailjet.tupled, CandidatMailjet.unapply)
  }

  val candidatsMailjetTable = TableQuery[CandidatMailjetTable]
  val getCandidatQuery = Compiled { candidatId: Rep[CandidatId] =>
    candidatsMailjetTable.filter(c => c.candidatId === candidatId)
  }

  class RecruteurMailjetTable(tag: Tag) extends Table[RecruteurMailjet](tag, "recruteurs_mailjet") {

    def id = column[Long]("id", O.PrimaryKey)

    def recruteurId = column[RecruteurId]("recruteur_id")

    def mailjetContactId = column[MailjetContactId]("mailjet_id")

    def email = column[Email]("email")

    def * = (recruteurId, mailjetContactId, email) <> (RecruteurMailjet.tupled, RecruteurMailjet.unapply)
  }

  val recruteursMailjetTable = TableQuery[RecruteurMailjetTable]
  val getRecruteurQuery = Compiled { recruteurId: Rep[RecruteurId] =>
    recruteursMailjetTable.filter(r => r.recruteurId === recruteurId)
  }

  def getCandidat(candidatId: CandidatId): Future[CandidatMailjet] =
    database.run(getCandidatQuery(candidatId).result.head)

  def ajouterCandidat(candidat: CandidatMailjet): Future[Unit] =
    database
      .run(candidatsMailjetTable.map(c => (c.candidatId, c.mailjetContactId, c.email))
        += (candidat.candidatId, candidat.mailjetContactId, candidat.email))
      .map(_ => ())

  def streamCandidats: Source[CandidatMailjet, NotUsed] = Source.fromPublisher {
    database.stream(
      candidatsMailjetTable
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

  def getRecruteur(recruteurId: RecruteurId): Future[RecruteurMailjet] =
    database.run(getRecruteurQuery(recruteurId).result.head)

  def ajouterRecruteur(recruteur: RecruteurMailjet): Future[Unit] =
    database
      .run(recruteursMailjetTable.map(r => (r.recruteurId, r.mailjetContactId, r.email))
        += (recruteur.recruteurId, recruteur.mailjetContactId, recruteur.email))
      .map(_ => ())
}
