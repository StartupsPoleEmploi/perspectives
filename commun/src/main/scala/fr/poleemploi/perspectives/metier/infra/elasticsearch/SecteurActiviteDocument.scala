package fr.poleemploi.perspectives.metier.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.CodeSecteurActivite
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class SecteurActiviteDocument(code: CodeSecteurActivite,
                                   label: String,
                                   sousSecteurs: List[SousSecteurDocument])

object SecteurActiviteDocument {

  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._

  implicit val reads: Reads[SecteurActiviteDocument] = (
    (JsPath \ "code").read[CodeSecteurActivite] and
      (JsPath \ "label").read[String] and
      (JsPath \ "sous_secteurs").read[List[SousSecteurDocument]]
    ) (SecteurActiviteDocument.apply _)
}
