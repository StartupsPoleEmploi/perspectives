package fr.poleemploi.perspectives.commun.infra.play.json

import fr.poleemploi.eventsourcing.{AggregateId, IntValueObject, StringValueObject}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.RecruteurId
import play.api.libs.json._

/**
  * Contient des macros Json.format : l'ordre de déclaration est important
  */
object JsonFormats {

  implicit val formatCandidatId: Format[CandidatId] = formatAggregateId(CandidatId)
  implicit val formatRecruteurId: Format[RecruteurId] = formatAggregateId(RecruteurId)
  implicit val formatCvId: Format[CVId] = formatStringValueObject(CVId)

  implicit val formatGenre: Format[Genre] = formatStringValueObject(Genre(_))
  implicit val formatCodeDepartement: Format[CodeDepartement] = formatStringValueObject(CodeDepartement)
  implicit val formatCodeROME: Format[CodeROME] = formatStringValueObject(CodeROME)
  implicit val formatCodeSecteurActivite: Format[CodeSecteurActivite] = formatStringValueObject(CodeSecteurActivite(_))
  implicit val formatEmail: Format[Email] = formatStringValueObject(Email)
  implicit val formatRayonRecherche: Format[RayonRecherche] = formatIntValueObject(RayonRecherche(_))
  implicit val formatNumeroTelephone: Format[NumeroTelephone] = formatStringValueObject(NumeroTelephone(_))
  implicit val formatStatutDemandeurEmploi: Format[StatutDemandeurEmploi] = formatStringValueObject(StatutDemandeurEmploi(_))
  implicit val formatTypeMedia: Format[TypeMedia] = formatStringValueObject(TypeMedia(_))
  implicit val formatHabilete: Format[Habilete] = formatStringValueObject(Habilete(_))

  implicit val formatDepartement: Format[Departement] = Json.format[Departement]
  implicit val formatMetier: Format[Metier] = Json.format[Metier]
  implicit val formatSecteurActivite: Format[SecteurActivite] = Json.format[SecteurActivite]

  def readsAggregateId[T <: AggregateId](deserialize: String => T): Reads[T] = Reads {
    case JsString(s) => JsSuccess(deserialize(s))
    case _ => JsError("Not a string")
  }

  def writesAggregateId[T <: AggregateId]: Writes[T] = Writes { id =>
    JsString(id.value)
  }

  def formatAggregateId[T <: AggregateId](deserialize: String => T): Format[T] =
    Format(readsAggregateId[T](deserialize), writesAggregateId[T])

  def readsStringValueObject[T <: StringValueObject](deserialize: String => T): Reads[T] = Reads {
    case JsString(s) => JsSuccess(deserialize(s))
    case _ => JsError("Not a string")
  }

  def writesStringValueObject[T <: StringValueObject]: Writes[T] = Writes { s =>
    JsString(s.value)
  }

  def formatStringValueObject[T <: StringValueObject](deserialize: String => T): Format[T] =
    Format(readsStringValueObject[T](deserialize), writesStringValueObject[T])

  def readsIntValueObject[T <: IntValueObject](deserialize: Int => T): Reads[T] = Reads {
    case JsNumber(n) => JsSuccess(deserialize(n.intValue()))
    case _ => JsError("Not a number")
  }

  def writesIntValueObject[T <: IntValueObject]: Writes[T] = Writes { i =>
    JsNumber(i.value)
  }

  def formatIntValueObject[T <: IntValueObject](deserialize: Int => T): Format[T] =
    Format(readsIntValueObject[T](deserialize), writesIntValueObject[T])
}
