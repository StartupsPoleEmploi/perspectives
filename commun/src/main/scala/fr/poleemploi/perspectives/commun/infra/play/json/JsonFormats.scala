package fr.poleemploi.perspectives.commun.infra.play.json

import fr.poleemploi.perspectives.commun.domain._
import play.api.libs.json.{JsString, Json, Writes}

object JsonFormats {

  implicit val jsonWritesCodeDepartement: Writes[CodeDepartement] = Writes { codeDepartement =>
    JsString(codeDepartement.value)
  }

  implicit val jsonWritesDepartement: Writes[Departement] = Json.writes[Departement]

  implicit val jsonWritesCodeROME: Writes[CodeROME] = Writes { codeROME =>
    JsString(codeROME.value)
  }

  implicit val jsonWritesMetier: Writes[Metier] = Json.writes[Metier]

  implicit val jsonWritesCodeSecteurActivite: Writes[CodeSecteurActivite] = Writes { codeSecteurActivite =>
    JsString(codeSecteurActivite.value)
  }

  implicit val jsonWritesSecteurActivite: Writes[SecteurActivite] = Json.writes[SecteurActivite]
}
