package fr.poleemploi.perspectives.commun.infra.sql

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.array.PgArrayExtensions
import fr.poleemploi.eventsourcing.{AggregateId, IntValueObject, StringValueObject}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import fr.poleemploi.perspectives.emailing.infra.mailjet.MailjetContactId
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

import scala.reflect.ClassTag

trait PostgresDriver extends ExPostgresProfile
  with PgArraySupport
  with PgArrayExtensions
  with PgDate2Support {

  protected override def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  object PostgresAPI extends API
    with ArrayImplicits
    with SimpleArrayPlainImplicits
    with DateTimeImplicits {

    implicit val recruteurIdColumnType: BaseColumnType[RecruteurId] = mapAggregateId(RecruteurId)

    implicit val candidatIdColumnType: BaseColumnType[CandidatId] = mapAggregateId(CandidatId)

    implicit val cvIdColumnType: BaseColumnType[CVId] = mapStringValueObject(CVId)

    implicit val peConnectIdColumnType: BaseColumnType[PEConnectId] = mapStringValueObject(PEConnectId)

    implicit val mailjetContactIdColumnType: BaseColumnType[MailjetContactId] = mapIntValueObject(MailjetContactId)

    implicit val typeRecruteurColumnType: BaseColumnType[TypeRecruteur] = mapStringValueObject(TypeRecruteur(_))

    implicit val genreColumnType: BaseColumnType[Genre] = mapStringValueObject(Genre(_))

    implicit val nomColumnType: BaseColumnType[Nom] = mapStringValueObject(Nom(_))

    implicit val prenomColumnType: BaseColumnType[Prenom] = mapStringValueObject(Prenom(_))

    implicit val emailColumnType: BaseColumnType[Email] = mapStringValueObject(Email)

    implicit val numeroSiretColumnType: BaseColumnType[NumeroSiret] = mapStringValueObject(NumeroSiret(_))

    implicit val numeroTelephoneColumnType: BaseColumnType[NumeroTelephone] = mapStringValueObject(NumeroTelephone(_))

    implicit val codeROMEColumnType: BaseColumnType[CodeROME] = mapStringValueObject(CodeROME)

    implicit val codeROMETypeMapper: DriverJdbcType[List[CodeROME]] = new SimpleArrayJdbcType[String]("text")
      .mapTo[CodeROME](CodeROME, _.value).to(_.toList)

    implicit val statutDemandeurEmploiColumnType: BaseColumnType[StatutDemandeurEmploi] = mapStringValueObject(StatutDemandeurEmploi(_))

    implicit val uniteMesureColumnType: BaseColumnType[UniteLongueur] = mapStringValueObject(UniteLongueur(_))

    implicit val typeMediaColumnType: BaseColumnType[TypeMedia] = mapStringValueObject(TypeMedia(_))

    implicit val codeSecteurActiviteColumnType: BaseColumnType[CodeSecteurActivite] = mapStringValueObject(CodeSecteurActivite)

    implicit val codeDepartementColumnType: BaseColumnType[CodeDepartement] = mapStringValueObject(CodeDepartement)

    implicit val habileteTypeMapper: DriverJdbcType[List[Habilete]] = new SimpleArrayJdbcType[String]("text")
      .mapTo[Habilete](Habilete, _.value).to(_.toList)

    def mapAggregateId[T <: AggregateId](deserialize: String => T)(implicit tag: ClassTag[T]): BaseColumnType[T] =
      MappedColumnType.base[T, String](
        { t => t.value },
        { s => deserialize(s) }
      )

    def mapStringValueObject[T <: StringValueObject](deserialize: String => T)(implicit tag: ClassTag[T]): BaseColumnType[T] =
      MappedColumnType.base[T, String](
        { t => t.value },
        { s => deserialize(s) }
      )

    def mapIntValueObject[T <: IntValueObject](deserialize: Int => T)(implicit tag: ClassTag[T]): BaseColumnType[T] =
      MappedColumnType.base[T, Int](
        { t => t.value },
        { s => deserialize(s) }
      )
  }

  override val api = PostgresAPI
}

object PostgresDriver extends PostgresDriver
