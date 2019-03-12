package fr.poleemploi.perspectives.commun.infra.play.json

import fr.poleemploi.eventsourcing.{AggregateId, IntValueObject, StringValueObject}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.{CandidatId, LocalisationRecherche, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.offre.domain.OffreId
import fr.poleemploi.perspectives.recruteur.alerte.domain.{AlerteId, FrequenceAlerte, LocalisationAlerte}
import fr.poleemploi.perspectives.recruteur.{NumeroSiret, RecruteurId, TypeRecruteur}
import play.api.libs.json._

/**
  * Contient des macros Json.format : l'ordre de déclaration est important
  */
object JsonFormats {

  implicit val formatCandidatId: Format[CandidatId] = formatAggregateId(CandidatId)
  implicit val formatRecruteurId: Format[RecruteurId] = formatAggregateId(RecruteurId)
  implicit val formatCvId: Format[CVId] = formatStringValueObject(CVId)
  implicit val formatOffreId: Format[OffreId] = formatStringValueObject(OffreId)
  implicit val formatAlerteId: Format[AlerteId] = formatStringValueObject(AlerteId)

  implicit val formatGenre: Format[Genre] = formatStringValueObject(Genre(_))
  implicit val formatNom: Format[Nom] = formatStringValueObject(Nom(_))
  implicit val formatPrenom: Format[Prenom] = formatStringValueObject(Prenom(_))
  implicit val formatCodeDepartement: Format[CodeDepartement] = formatStringValueObject(CodeDepartement)
  implicit val formatEmail: Format[Email] = formatStringValueObject(Email)
  implicit val formatUniteMesure: Format[UniteLongueur] = formatStringValueObject(UniteLongueur(_))
  implicit val formatRayonRecherche: Format[RayonRecherche] = Json.format[RayonRecherche]
  implicit val formatNumeroTelephone: Format[NumeroTelephone] = formatStringValueObject(NumeroTelephone(_))
  implicit val formatStatutDemandeurEmploi: Format[StatutDemandeurEmploi] = formatStringValueObject(StatutDemandeurEmploi(_))
  implicit val formatTypeMedia: Format[TypeMedia] = formatStringValueObject(TypeMedia(_))
  implicit val formatFrequenceAlerte: Format[FrequenceAlerte] = formatStringValueObject(FrequenceAlerte(_))

  implicit val formatCodeROME: Format[CodeROME] = formatStringValueObject(CodeROME)
  implicit val formatCodeSecteurActivite: Format[CodeSecteurActivite] = formatStringValueObject(CodeSecteurActivite)
  implicit val formatCodeDomaineProfessionnel: Format[CodeDomaineProfessionnel] = formatStringValueObject(CodeDomaineProfessionnel)
  implicit val formatHabilete: Format[Habilete] = formatStringValueObject(Habilete)

  implicit val formatCoordonnees: Format[Coordonnees] = Json.format[Coordonnees]
  implicit val formatLocalisation: Format[LocalisationAlerte] = Json.format[LocalisationAlerte]
  implicit val formatLocalisationRecherche: Format[LocalisationRecherche] = Json.format[LocalisationRecherche]

  implicit val formatTypeRecruteur: Format[TypeRecruteur] = formatStringValueObject(TypeRecruteur(_))
  implicit val formatNumeroSiret: Format[NumeroSiret] = formatStringValueObject(NumeroSiret(_))

  def readsAggregateId[T <: AggregateId](deserialize: String => T): Reads[T] = Reads {
    case JsString(s) => JsSuccess(deserialize(s))
    case _ => JsError("Not a string")
  }

  def writesAggregateId[T <: AggregateId]: Writes[T] = Writes(id => JsString(id.value))

  def formatAggregateId[T <: AggregateId](deserialize: String => T): Format[T] =
    Format(readsAggregateId[T](deserialize), writesAggregateId[T])

  def readsStringValueObject[T <: StringValueObject](deserialize: String => T): Reads[T] = Reads {
    case JsString(s) => JsSuccess(deserialize(s))
    case _ => JsError("Not a string")
  }

  def writesStringValueObject[T <: StringValueObject]: Writes[T] = Writes(s => JsString(s.value))

  def formatStringValueObject[T <: StringValueObject](deserialize: String => T): Format[T] =
    Format(readsStringValueObject[T](deserialize), writesStringValueObject[T])

  def readsIntValueObject[T <: IntValueObject](deserialize: Int => T): Reads[T] = Reads {
    case JsNumber(n) => JsSuccess(deserialize(n.intValue()))
    case _ => JsError("Not a number")
  }

  def writesIntValueObject[T <: IntValueObject]: Writes[T] = Writes(i => JsNumber(i.value))

  def formatIntValueObject[T <: IntValueObject](deserialize: Int => T): Format[T] =
    Format(readsIntValueObject[T](deserialize), writesIntValueObject[T])
}
