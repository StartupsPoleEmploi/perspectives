package fr.poleemploi.perspectives.metier.infra.file

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Metier}
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.io.{BufferedSource, Source}

/**
  * Charge le fichier contenant les métiers en json. <br />
  * Implémentation qui peut être utilisée pendant le développement si on ne veut pas appeler le webservice à chaque démarrage d'application.
  */
class ReferentielMetierFileAdapter extends ReferentielMetier {

  val source: BufferedSource = Source.fromFile(getClass.getResource("metiers.json").toURI)
  val metiers: Map[CodeROME, Metier] =
    Json.fromJson[List[MetierFileDto]](Json.parse(source.mkString)) match {
      case s: JsSuccess[List[MetierFileDto]] =>
        s.value.foldLeft(Map[CodeROME, Metier]())(
          (map, json) => map + (json.codeROME -> new Metier(codeROME = json.codeROME, label = json.label, habiletes = Nil))
        )
      case e: JsError => throw new RuntimeException(s"Impossible de charger le referentiel metier : $e")
    }

  /**
    * Renvoie une exception si le Métier n'est associé à aucun code.
    */
  override def metierParCode(code: CodeROME): Metier =
    metiers.getOrElse(code, throw new IllegalArgumentException(s"Aucun label associé au code : $code"))

}
