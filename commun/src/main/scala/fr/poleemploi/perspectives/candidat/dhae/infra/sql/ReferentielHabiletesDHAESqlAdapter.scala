package fr.poleemploi.perspectives.candidat.dhae.infra.sql

import fr.poleemploi.perspectives.candidat.dhae.domain.HabiletesDHAE
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}
import fr.poleemploi.perspectives.commun.infra.sql.PostgresDriver
import slick.jdbc.JdbcBackend.Database
import slick.lifted.{Constraint, PrimaryKey}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReferentielHabiletesDHAESqlAdapter(val driver: PostgresDriver,
                                         database: Database) {

  import driver.api._

  class HabiletesDHAETable(tag: Tag) extends Table[HabiletesDHAE](tag, "dhae_habiletes") {

    def id = column[Long]("id", O.PrimaryKey)

    def codeROME = column[CodeROME]("code_rome")

    def codeDepartement = column[CodeDepartement]("code_departement")

    def habiletes = column[List[Habilete]]("habiletes")

    def pk: PrimaryKey = primaryKey("dhae_habiletes_pk", id)

    override def tableConstraints: Iterator[Constraint] = List(primaryKey("dhae_habiletes_unicite_dhae", (codeROME, codeDepartement))).toIterator

    def * = (codeROME, codeDepartement, habiletes) <> (HabiletesDHAE.tupled, HabiletesDHAE.unapply)
  }

  val habiletesDHAETable = TableQuery[HabiletesDHAETable]

  def ajouter(habiletesDHAE: Stream[HabiletesDHAE]): Future[Int] = {
    val bulkInsert: DBIO[Option[Int]] = habiletesDHAETable.map(
      h => (h.codeROME, h.codeDepartement, h.habiletes)
    ) insertOrUpdateAll habiletesDHAE.map(
      h => (h.codeROME, h.codeDepartement, h.habiletes)
    )

    database.run(bulkInsert).map(_.getOrElse(0))
  }
}