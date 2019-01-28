package fr.poleemploi.perspectives.candidat.mrs.infra.sql

import fr.poleemploi.perspectives.candidat.mrs.domain.{HabiletesMRS, ReferentielHabiletesMRS}
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}
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

    def codeDepartement = column[CodeDepartement]("code_departement")

    def habiletes = column[List[Habilete]]("habiletes")

    def pk: PrimaryKey = primaryKey("mrs_habiletes_pk", id)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("mrs_habiletes_unicite_mrs", (codeROME, codeDepartement))).toIterator

    def * = (codeROME, codeDepartement, habiletes) <> (HabiletesMRS.tupled, HabiletesMRS.unapply)
  }

  val habiletesMRSTable = TableQuery[HabiletesMRSTable]

  val habiletesParMRSQuery = Compiled { (codeROME: Rep[CodeROME], codeDepartement: Rep[CodeDepartement]) =>
    habiletesMRSTable
      .filter(h => h.codeROME === codeROME && h.codeDepartement === codeDepartement)
  }

  def ajouter(habiletesMRS: Stream[HabiletesMRS]): Future[Int] = {
    val bulkInsert: DBIO[Option[Int]] = habiletesMRSTable.map(
      h => (h.codeROME, h.codeDepartement, h.habiletes)
    ) insertOrUpdateAll habiletesMRS.map(
      h => (h.codeROME, h.codeDepartement, h.habiletes)
    )

    database.run(bulkInsert).map(_.getOrElse(0))
  }

  override def habiletes(codeROME: CodeROME, codeDepartement: CodeDepartement): Future[List[Habilete]] =
    database.run(habiletesParMRSQuery(codeROME, codeDepartement).result.headOption).map(_.map(
      h => h.habiletes
    ).getOrElse(Nil))

  override def codeROMEsParDepartement: Future[Map[CodeDepartement, List[CodeROME]]] =
    database.run(habiletesMRSTable.map(h => (h.codeDepartement, h.codeROME)).result)
      .map(_.toList.groupBy(_._1).map(v => (v._1, v._2.map(l => l._2))))
}
