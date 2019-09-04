package fr.poleemploi.perspectives.authentification.infra.autologin

import fr.poleemploi.eventsourcing.StringValueObject
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import play.api.libs.json.{Format, Json}

case class AutologinToken(identifiant: String,
                          nom: Nom,
                          prenom: Prenom,
                          typeUtilisateur: TypeUtilisateur)

object AutologinToken {
  import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
  implicit val typeUtilisateurFormat: Format[TypeUtilisateur] = Json.format[TypeUtilisateur]
  implicit val jsonFormat: Format[AutologinToken] = Json.format[AutologinToken]
}


case class TypeUtilisateur(value: String) extends StringValueObject

object TypeUtilisateur {

  val CANDIDAT = TypeUtilisateur("candidat")
  val RECRUTEUR = TypeUtilisateur("recruteur")
  val CONSEILLER = TypeUtilisateur("conseiller")

}
