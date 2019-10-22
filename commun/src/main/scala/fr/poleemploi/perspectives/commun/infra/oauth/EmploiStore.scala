package fr.poleemploi.perspectives.commun.infra.oauth

import fr.poleemploi.perspectives.commun.infra.Environnement

import scala.collection.mutable.ListBuffer

sealed trait QualityOfService

object QualityOfService {

  case object BRONZE extends QualityOfService

  case object SILVER extends QualityOfService

  case object GOLD extends QualityOfService

}

case class EmploiStoreAPI(name: String,
                          version: String,
                          qos: QualityOfService,
                          scopes: List[OauthScope] = Nil) {

  val mainScope: OauthScope = OauthScope(s"api_$name$version")

}

object EmploiStoreAPI {

  import QualityOfService._

  def buildQOSScope(api: EmploiStoreAPI): Option[OauthScope] = api.qos match {
    case BRONZE => None
    case SILVER => Some(OauthScope(s"qos_silver_${api.name}${api.version}"))
    case GOLD => Some(OauthScope(s"qos_gold_${api.name}${api.version}"))
  }
}

class EmploiStoreOauthScopeBuilder(environnement: Environnement) {

  private var apis: ListBuffer[EmploiStoreAPI] = ListBuffer()

  def avecApiIndividu: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-individu",
      version = "v1",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(
        OauthScope("openid"),
        OauthScope("profile"),
        OauthScope("email")
      )
    )

    this
  }

  def avecApiCoordonnees: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-coordonnees",
      version = "v1",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(OauthScope("coordonnees"))
    )

    this
  }

  def avecApiStatut: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-statut",
      version = "v1",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(OauthScope("statut"))
    )

    this
  }

  def avecApiPrestations: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "prestationssuivies",
      version = "v1",
      qos = QualityOfService.BRONZE,
      scopes = List(OauthScope("prestationIntermediaire"))
    )

    this
  }

  def avecApiCompetences: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-competences",
      version = "v2",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(
        OauthScope("pfccompetences"),
        OauthScope("pfclangues"),
        OauthScope("pfccentresinteret")
      )
    )

    this
  }

  def avecApiFormations: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-formations",
      version = "v1",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(
        OauthScope("pfcformations"),
        OauthScope("pfcpermis")
      )
    )

    this
  }

  def avecApiExperiencesProfessionnelles: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-experiences",
      version = "v1",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(OauthScope("pfcexperiences"))
    )

    this
  }

  def avecApiEntreprise: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "peconnect-entreprise",
      version = "v1",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(
        OauthScope("openid"),
        OauthScope("profile"),
        OauthScope("email"),
        OauthScope("habilitation")
      )
    )

    this
  }

  def avecApiOffre: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "offresdemploi",
      version = "v2",
      qos =
        if (Environnement.PRODUCTION == environnement)
          QualityOfService.SILVER
        else
          QualityOfService.BRONZE,
      scopes = List(OauthScope("o2dsoffre"))
    )

    this
  }

  def avecApiRome: EmploiStoreOauthScopeBuilder = {
    apis += EmploiStoreAPI(
      name = "rome",
      version = "v1",
      qos = QualityOfService.BRONZE,
      scopes = List(
        OauthScope("api_romev1"),
        OauthScope("nomenclatureRome")
      )
    )

    this
  }

  def build: List[OauthScope] = {
    val scopes = apis.foldLeft(List[OauthScope]())((acc, api) =>
      acc ++ (api.mainScope :: api.scopes) ++ List(EmploiStoreAPI.buildQOSScope(api)).flatten
    )
    apis = ListBuffer()
    scopes
  }
}
