package fr.poleemploi.perspectives.commun.infra.sql

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.array.PgArrayExtensions
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}

trait PostgresDriver extends ExPostgresProfile
  with PgArraySupport
  with PgArrayExtensions
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
      { id => id.value },
      { s => CVId(s) }
    )

    implicit val peConnectIdColumnType: BaseColumnType[PEConnectId] = MappedColumnType.base[PEConnectId, String](
      { id => id.value },
      { s => PEConnectId(s) }
    )

    implicit val mailjetContactIdColumnType: BaseColumnType[MailjetContactId] = MappedColumnType.base[MailjetContactId, Int](
      { id => id.value },
      { s => MailjetContactId(s) }
    )

    implicit val typeRecruteurColumnType: BaseColumnType[TypeRecruteur] = MappedColumnType.base[TypeRecruteur, String](
      { t => t.value },
      { s => TypeRecruteur(s) }
    )

    implicit val genreColumnType: BaseColumnType[Genre] = MappedColumnType.base[Genre, String](
      { g => g.value },
      { s => Genre(s) }
    )

    implicit val emailColumnType: BaseColumnType[Email] = MappedColumnType.base[Email, String](
      { e => e.value },
      { s => Email(s) }
    )

    implicit val numeroSiretColumnType: BaseColumnType[NumeroSiret] = MappedColumnType.base[NumeroSiret, String](
      { n => n.value },
      { s => NumeroSiret(s) }
    )

    implicit val numeroTelephoneColumnType: BaseColumnType[NumeroTelephone] = MappedColumnType.base[NumeroTelephone, String](
      { n => n.value },
      { s => NumeroTelephone(s) }
    )

    implicit val codeROMEColumnType: BaseColumnType[CodeROME] = MappedColumnType.base[CodeROME, String](
      { c => c.value },
      { s => CodeROME(s) }
    )

    implicit val listCodeROMEColumnType: BaseColumnType[List[CodeROME]] = MappedColumnType.base[List[CodeROME], List[String]](
      { c => c.map(_.value) },
      { s => s.map(CodeROME) }
    )

    implicit val statutDemandeurEmploiColumnType: BaseColumnType[StatutDemandeurEmploi] = MappedColumnType.base[StatutDemandeurEmploi, String](
      { st => st.value },
      { s => StatutDemandeurEmploi(s) }
    )

    implicit val rayonRechercheColumnType: BaseColumnType[RayonRecherche] = MappedColumnType.base[RayonRecherche, Int](
      { r => r.value },
      { i => RayonRecherche(i) }
    )

    implicit val typeMediaColumnType: BaseColumnType[TypeMedia] = MappedColumnType.base[TypeMedia, String](
      { t => t.value },
      { s => TypeMedia(s) }
    )
  }

  override val api = PostgresAPI
}

object PostgresDriver extends PostgresDriver
