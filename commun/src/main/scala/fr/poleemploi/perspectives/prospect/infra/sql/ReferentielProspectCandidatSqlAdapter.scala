package fr.poleemploi.perspectives.prospect.infra.sql

import java.time.LocalDate

import akka.NotUsed
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import fr.poleemploi.perspectives.metier.domain.Metier
import fr.poleemploi.perspectives.prospect.domain.{ProspectCandidat, ReferentielProspectCandidat}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.lifted.{Constraint, PrimaryKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielProspectCandidatSqlAdapter(val driver: PostgresDriver,
                                            database: Database) extends ReferentielProspectCandidat {

  import driver.api._

  class ProspectCandidatTable(tag: Tag) extends Table[ProspectCandidatRecord](tag, "prospects_candidats") {

    def id = column[Long]("id", O.PrimaryKey)

    def peConnectId = column[PEConnectId]("peconnect_id")

    def identifiantLocal = column[IdentifiantLocal]("identifiant_local")

    def codeNeptune = column[Option[CodeNeptune]]("code_neptune")

    def nom = column[Nom]("nom")

    def prenom = column[Prenom]("prenom")

    def email = column[Email]("email")

    def genre = column[Genre]("genre")

    def codeDepartement = column[CodeDepartement]("code_departement")

    def codeRomeMrs = column[CodeROME]("code_rome_mrs")

    def metierMrs = column[String]("metier_mrs")

    def dateEvaluationMrs = column[LocalDate]("date_evaluation_mrs")

    def pk: PrimaryKey = primaryKey("prospects_candidats_pk", id)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("prospect_candidat_unique_key", (peConnectId, identifiantLocal, codeRomeMrs))).toIterator

    def * = (peConnectId, identifiantLocal, codeNeptune, nom, prenom, email, genre, codeDepartement, codeRomeMrs, metierMrs, dateEvaluationMrs) <> (ProspectCandidatRecord.tupled, ProspectCandidatRecord.unapply)
  }

  val prospectCandidatTable = TableQuery[ProspectCandidatTable]

  private val findByEmailQuery = Compiled { email: Rep[Email] =>
    prospectCandidatTable.filter(_.email === email)
  }

  override def streamProspectsCandidats(dateMaxEvaluationMrs: Option[LocalDate]): Source[ProspectCandidat, NotUsed] = Source.fromPublisher {
    database.stream(
      prospectCandidatTable
        .filter(_.dateEvaluationMrs <= dateMaxEvaluationMrs.getOrElse(LocalDate.now))
        .sortBy(_.id)
        .result
        .transactionally
        .withStatementParameters(
          rsType = ResultSetType.ForwardOnly,
          rsConcurrency = ResultSetConcurrency.ReadOnly,
          fetchSize = 1000
        )
    ).mapResult(buildProspectCandidat)
  }

  override def ajouter(prospectsCandidats: Stream[ProspectCandidat]): Future[Unit] = {
    val bulkInsert: DBIO[Option[Int]] = prospectCandidatTable.map(
      m => (m.peConnectId, m.identifiantLocal, m.codeNeptune, m.nom, m.prenom, m.email, m.genre, m.codeDepartement, m.codeRomeMrs, m.metierMrs, m.dateEvaluationMrs)
    ) insertOrUpdateAll prospectsCandidats.map(
      m => (m.peConnectId, m.identifiantLocal, m.codeNeptune, m.nom, m.prenom, m.email, m.genre, m.codeDepartement, m.metier.codeROME, m.metier.label, m.dateEvaluation)
    )

    database.run(bulkInsert).map(_ => ())
  }

  override def supprimer(email: Email): Future[Unit] = {
    val deleteQuery = prospectCandidatTable.filter(_.email === email).delete
    database.run(deleteQuery).map(_ => ())
  }

  override def find(email: Email): Future[Option[ProspectCandidat]] =
    database.run(findByEmailQuery(email).result)
      .map(_.headOption.map(buildProspectCandidat))

  private def buildProspectCandidat(x: ProspectCandidatRecord) = ProspectCandidat(
    peConnectId = x.peConnectId,
    identifiantLocal = x.identifiantLocal,
    codeNeptune = x.codeNeptune,
    nom = x.nom,
    prenom = x.prenom,
    email = x.email,
    genre = x.genre,
    codeDepartement = x.codeDepartement,
    metier = Metier(
      codeROME = x.codeRomeMrs,
      label = x.metierMrs
    ),
    dateEvaluation = x.dateEvaluationMrs
  )

}
