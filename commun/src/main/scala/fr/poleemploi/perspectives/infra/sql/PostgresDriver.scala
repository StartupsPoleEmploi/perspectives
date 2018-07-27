package fr.poleemploi.perspectives.infra.sql

import com.github.tminglei.slickpg._
import fr.poleemploi.perspectives.domain.candidat.CandidatId
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone}

trait PostgresDriver extends ExPostgresProfile
  with PgArraySupport
  with PgDate2Support {

  object PostgresAPI extends API
    with ArrayImplicits
    with DateTimeImplicits {

    implicit val strListTypeMapper: DriverJdbcType[List[String]] = new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val recruteurIdColumnType: BaseColumnType[RecruteurId] = MappedColumnType.base[RecruteurId, String](
      { id => id.value },
      { s => RecruteurId(s) }
    )

    implicit val candidatIdColumnType: BaseColumnType[CandidatId] = MappedColumnType.base[CandidatId, String](
      { id => id.value },
      { s => CandidatId(s) }
    )

    implicit val cvIdColumnType: BaseColumnType[CVId] = MappedColumnType.base[CVId, String](
      { id => id.value},
      { s => CVId(s)}
    )

    implicit val typeRecruteurColumnType: BaseColumnType[TypeRecruteur] = MappedColumnType.base[TypeRecruteur, String](
      { t => t.value },
      { s => TypeRecruteur.from(s).get }
    )

    implicit val genreColumnType: BaseColumnType[Genre] = MappedColumnType.base[Genre, String](
      { g => g.value },
      { s => Genre.from(s).get }
    )

    implicit val numeroSiretColumnType: BaseColumnType[NumeroSiret] = MappedColumnType.base[NumeroSiret, String](
      { n => n.value },
      { s => NumeroSiret.from(s).get }
    )

    implicit val numeroTelephoneColumnType: BaseColumnType[NumeroTelephone] = MappedColumnType.base[NumeroTelephone, String](
      { n => n.value },
      { s => NumeroTelephone.from(s).get }
    )

    implicit val setMetiersColumnType: BaseColumnType[Set[Metier]] = MappedColumnType.base[Set[Metier], List[String]](
      { m => m.map(_.value).toList },
      { s => s.flatMap(Metier.from).toSet }
    )
  }

  override val api = PostgresAPI
}

object PostgresDriver extends PostgresDriver
