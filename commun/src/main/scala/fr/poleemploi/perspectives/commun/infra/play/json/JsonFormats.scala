package fr.poleemploi.perspectives.commun.infra.play.json

import fr.poleemploi.eventsourcing.{AggregateId, IntValueObject, StringValueObject}
import fr.poleemploi.perspectives.candidat.cv.domain.{CVId, TypeMedia}
import fr.poleemploi.perspectives.candidat.{CandidatId, StatutDemandeurEmploi}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.recruteur.RecruteurId
import play.api.libs.json._

object JsonFormats {

  implicit val writesCandidatId: Writes[CandidatId] = writesAggregateId[CandidatId]
  implicit val writesRecruteurId: Writes[RecruteurId] = writesAggregateId[RecruteurId]
  implicit val writesCvId: Writes[CVId] = writesStringValueObject[CVId]

  implicit val writesGenre: Writes[Genre] = writesStringValueObject[Genre]
  implicit val writesCodeDepartement: Writes[CodeDepartement] = writesStringValueObject[CodeDepartement]
  implicit val writesCodeROME: Writes[CodeROME] = writesStringValueObject[CodeROME]
  implicit val writesCodeSecteurActivite: Writes[CodeSecteurActivite] = writesStringValueObject[CodeSecteurActivite]
  implicit val writesEmail: Writes[Email] = writesStringValueObject[Email]
  implicit val writesRayonRecherche: Writes[RayonRecherche] = writesIntValueObject[RayonRecherche]
  implicit val writesNumeroTelephone: Writes[NumeroTelephone] = writesStringValueObject[NumeroTelephone]
  implicit val writesStatutDemandeurEmploi: Writes[StatutDemandeurEmploi] = writesStringValueObject[StatutDemandeurEmploi]
  implicit val writesTypeMedia: Writes[TypeMedia] = writesStringValueObject[TypeMedia]

  implicit val jsonWritesDepartement: Writes[Departement] = Json.writes[Departement]
  implicit val jsonWritesMetier: Writes[Metier] = Json.writes[Metier]
  implicit val jsonWritesSecteurActivite: Writes[SecteurActivite] = Json.writes[SecteurActivite]

  private def writesAggregateId[T <: AggregateId]: Writes[T] = Writes { id =>
    JsString(id.value)
  }

  private def writesStringValueObject[T <: StringValueObject]: Writes[T] = Writes { s =>
    JsString(s.value)
  }

  private def writesIntValueObject[T <: IntValueObject]: Writes[T] = Writes { i =>
    JsNumber(i.value)
  }
}
