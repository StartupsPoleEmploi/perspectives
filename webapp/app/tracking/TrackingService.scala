package tracking

import controllers.FlashMessages._
import fr.poleemploi.perspectives.authentification.domain.{CandidatAuthentifie, RecruteurAuthentifie}
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json.{JsArray, JsNumber, JsObject, Json}
import play.api.mvc.Flash

object TrackingService {

  val event = "event"
  val eventCandidatConnecte = "candidat_connecte"
  val eventCandidatDeconnecte = "candidat_deconnecte"
  val eventCandidatAutologue = "candidat_autologue"
  val eventCandidatInscrit = "candidat_inscrit"
  val eventRecruteurConnecte = "recruteur_connecte"
  val eventRecruteurDeconnecte = "recruteur_deconnecte"
  val eventRecruteurInscrit = "recruteur_inscrit"
  val candidatId = "candidat_id"
  val recruteurId = "recruteur_id"
  val email = "email"
  val isConnecte = "is_connecte"
  val typeUtilisateur = "type_utilisateur"
  val typeUtilisateurCandidat = "candidat"
  val typeUtilisateurRecruteur = "recruteur"
  val typeRecruteur = "type_recruteur"
  val typeRecruteurOrganismeFormation = "organisme_formation"
  val typeRecruteurAgenceInterim = "agence_interim"
  val typeRecruteurEntreprise = "entreprise"
  val isRecruteurCertifie = "is_certifie"

  def buildTrackingCommun(optCandidatAuthentifie: Option[CandidatAuthentifie],
                          optRecruteurAuthentifie: Option[RecruteurAuthentifie]): JsArray =
    if (optCandidatAuthentifie.isDefined)
      buildTrackingCandidat(optCandidatAuthentifie)
    else if (optRecruteurAuthentifie.isDefined)
      buildTrackingRecruteur(optRecruteurAuthentifie)
    else Json.arr()

  def buildTrackingCandidat(optCandidatAuthentifie: Option[CandidatAuthentifie],
                            flash: Option[Flash] = None): JsArray = {
    val jsonCandidatId = optCandidatAuthentifie.map(x => Json.obj(
      candidatId -> x.candidatId.value
    )).getOrElse(Json.obj())

    val jsonCandidatEmail = optCandidatAuthentifie.flatMap(_.email.map(x => Json.obj(
      email -> x.value
    ))).getOrElse(Json.obj())

    val jsonCandidatEvent = buildTrackingEvenementCandidat(
      candidatInscrit = flash.exists(_.candidatInscrit),
      candidatConnecte = flash.exists(_.candidatConnecte),
      candidatDeconnecte = flash.exists(_.candidatDeconnecte),
      candidatAutologue = flash.exists(_.candidatAutologue)
    )

    Json.arr(Json.obj(
      isConnecte -> JsNumber(optCandidatAuthentifie.map(_ => BigDecimal(1)).getOrElse(BigDecimal(0))),
      typeUtilisateur -> typeUtilisateurCandidat
    ) ++ jsonCandidatId ++ jsonCandidatEmail ++ jsonCandidatEvent)
  }

  def buildTrackingRecruteur(optRecruteurAuthentifie: Option[RecruteurAuthentifie],
                             flash: Option[Flash] = None): JsArray = {
    val jsonRecruteurInfos = optRecruteurAuthentifie.map(x => Json.obj(
      recruteurId -> x.recruteurId.value,
      email -> x.email.value,
      isRecruteurCertifie -> JsNumber(if(x.certifie) 1 else 0)
    )).getOrElse(Json.obj())

    val jsonRecruteurEvent = buildTrackingEvenementRecruteur(
      recruteurInscrit = flash.exists(_.recruteurInscrit),
      recruteurConnecte = flash.exists(_.recruteurConnecte),
      recruteurDeconnecte = flash.exists(_.recruteurDeconnecte)
    )

    val jsonTypeRecruteur = (for {
      recruteurAuthentifie <- optRecruteurAuthentifie
      tr <- recruteurAuthentifie.typeRecruteur
      t <- buildTypeRecruteur(tr)
    } yield Json.obj(typeRecruteur -> t))
      .getOrElse(Json.obj())

    Json.arr(Json.obj(
      isConnecte -> JsNumber(optRecruteurAuthentifie.map(_ => BigDecimal(1)).getOrElse(BigDecimal(0))),
      typeUtilisateur -> typeUtilisateurRecruteur
    ) ++ jsonRecruteurInfos ++ jsonTypeRecruteur ++ jsonRecruteurEvent)
  }

  private def buildTypeRecruteur(typeRecruteur: TypeRecruteur): Option[String] =
    typeRecruteur match {
      case TypeRecruteur.ENTREPRISE => Some(typeRecruteurEntreprise)
      case TypeRecruteur.AGENCE_INTERIM => Some(typeRecruteurAgenceInterim)
      case TypeRecruteur.ORGANISME_FORMATION => Some(typeRecruteurOrganismeFormation)
      case _ => None
    }

  private def buildTrackingEvenementCandidat(candidatInscrit: Boolean,
                                             candidatConnecte: Boolean,
                                             candidatDeconnecte: Boolean,
                                             candidatAutologue: Boolean): JsObject = {
    if (candidatConnecte) Json.obj(event -> eventCandidatConnecte)
    else if (candidatInscrit) Json.obj(event -> eventCandidatInscrit)
    else if (candidatDeconnecte) Json.obj(event -> eventCandidatDeconnecte)
    else if (candidatAutologue) Json.obj(event -> eventCandidatAutologue)
    else Json.obj()
  }

  private def buildTrackingEvenementRecruteur(recruteurInscrit: Boolean,
                                              recruteurConnecte: Boolean,
                                              recruteurDeconnecte: Boolean): JsObject = {
    if (recruteurConnecte) Json.obj(event -> eventRecruteurConnecte)
    else if (recruteurInscrit) Json.obj(event -> eventRecruteurInscrit)
    else if (recruteurDeconnecte) Json.obj(event -> eventRecruteurDeconnecte)
    else Json.obj()
  }

}
