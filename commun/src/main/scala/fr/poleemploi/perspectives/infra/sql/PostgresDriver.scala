package fr.poleemploi.perspectives.infra.sql

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.array.PgArrayExtensions
import fr.poleemploi.perspectives.domain.authentification.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.domain.candidat.cv.CVId
import fr.poleemploi.perspectives.domain.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.domain.emailing.infra.mailjet.MailjetContactId
import fr.poleemploi.perspectives.domain.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import fr.poleemploi.perspectives.domain.{Genre, Metier, NumeroTelephone, RayonRecherche}

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

    implicit val metierColumnType: BaseColumnType[Metier] = MappedColumnType.base[Metier, String](
      { m => m.value },
      { s => Metier.from(s).get }
    )

    implicit val listMetiersColumnType: BaseColumnType[List[Metier]] = MappedColumnType.base[List[Metier], List[String]](
      { m => m.map(_.value) },
      { s => s.map(code => Metier.from(code).getOrElse(Metier(code, ""))) } // FIXME : referentiel métier
    )

    implicit val statutDemandeurEmploiColumnType: BaseColumnType[StatutDemandeurEmploi] = MappedColumnType.base[StatutDemandeurEmploi, String](
      { st => st.value },
      { s => StatutDemandeurEmploi.from(s).get }
    )

    implicit val rayonRechercheColumnType: BaseColumnType[RayonRecherche] = MappedColumnType.base[RayonRecherche, Int](
      { r => r.value },
      { s => RayonRecherche.from(s).get }
    )
  }

  override val api = PostgresAPI
}

object PostgresDriver extends PostgresDriver
