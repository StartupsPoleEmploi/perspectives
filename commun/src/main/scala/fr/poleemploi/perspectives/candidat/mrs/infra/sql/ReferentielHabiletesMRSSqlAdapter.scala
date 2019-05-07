package fr.poleemploi.perspectives.candidat.mrs.infra.sql

import fr.poleemploi.perspectives.candidat.mrs.domain.{HabiletesMRS, ReferentielHabiletesMRS}
import fr.poleemploi.perspectives.commun.domain.{CodeROME, Habilete}
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database
import slick.lifted.{Constraint, PrimaryKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielHabiletesMRSSqlAdapter(val driver: PostgresDriver,
                                        database: Database) extends ReferentielHabiletesMRS {

  import driver.api._

  class HabiletesMRSTable(tag: Tag) extends Table[HabiletesMRS](tag, "mrs_habiletes") {

    def id = column[Long]("id", O.PrimaryKey)

    def codeROME = column[CodeROME]("code_rome")

    def habiletes = column[List[Habilete]]("habiletes")

    def pk: PrimaryKey = primaryKey("mrs_habiletes_pk", id)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("mrs_habiletes_code_rome_key", codeROME)).toIterator

    def * = (codeROME, habiletes) <> (HabiletesMRS.tupled, HabiletesMRS.unapply)
  }

  val habiletesMRSTable = TableQuery[HabiletesMRSTable]

  val habiletesParMRSQuery = Compiled { codeROME: Rep[CodeROME] =>
    habiletesMRSTable
      .filter(h => h.codeROME === codeROME)
  }

  def ajouter(habiletesMRS: Stream[HabiletesMRS]): Future[Int] = {
    val bulkInsert: DBIO[Option[Int]] = habiletesMRSTable.map(
      h => (h.codeROME, h.habiletes)
    ) insertOrUpdateAll habiletesMRS.map(
      h => (h.codeROME, h.habiletes)
    )

    database.run(bulkInsert).map(_.getOrElse(0))
  }

  override def habiletes(codeROME: CodeROME): Future[Set[Habilete]] =
    database.run(habiletesParMRSQuery(codeROME).result.headOption).map(_.map(
      h => h.habiletes.toSet
    ).getOrElse(Set.empty))

  override def codeROMEsAvecHabiletes: Future[List[CodeROME]] =
    database.run(
      habiletesMRSTable.map(h => h.codeROME)
        .sortBy(h => h.value)
        .result
    ).map(_.toList)
}
