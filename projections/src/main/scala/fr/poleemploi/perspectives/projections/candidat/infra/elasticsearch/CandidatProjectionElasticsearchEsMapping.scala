package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.commun.domain.{CodeROME, Coordonnees}
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json._

object CandidatProjectionElasticsearchEsMapping {

  val candidat_id = "candidat_id"
  val nom = "nom"
  val prenom = "prenom"
  val genre = "genre"
  val email = "email"
  val recherche_metiers_evalues = "recherche_metiers_evalues"
  val recherche_autres_metiers = "recherche_autres_metiers"
  val date_inscription = "date_inscription"
  val date_derniere_connexion = "date_derniere_connexion"
  val metiers_recherches = "metiers_recherches"
  val metiers_evalues = "metiers_evalues"
  val habiletes = "habiletes"
  val contacte_par_agence_interim = "contacte_par_agence_interim"
  val contacte_par_organisme_formation = "contacte_par_organisme_formation"
  val rayon_recherche = "rayon_recherche"
  val numero_telephone = "numero_telephone"
  val statut_demandeur_emploi = "statut_demandeur_emploi"
  val cv_id = "cv_id"
  val cv_type_media = "cv_type_media"
  val code_postal = "code_postal"
  val commune = "commune"
  val mobilite = "mobilite"

  def buildQueryRechercherCandidatsParLocalisation(query: RechercherCandidatsQuery): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "filter" -> JsArray(
            Seq(
              filtreTypeRecruteur(query.typeRecruteur),
              Some(filtreRechercheMetier),
              Some(filtreMetiersEvalues),
              Some(filtreNumeroTelephone),
              Some(filtreCommune),
              query.coordonnees.map(c => filtreLocalisation(c))
            ).flatten
          )
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          Some(JsNumber(keysetPagination.dateInscription)),
          keysetPagination.candidatId.map(c => JsString(c.value))
        ).flatten)
      )
    ).getOrElse(queryJson)
  }

  def buildQueryRechercherCandidatsParSecteur(query: RechercherCandidatsQuery,
                                              metiers: List[CodeROME]): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "function_score" -> Json.obj(
          "query" -> Json.obj(
            "bool" -> Json.obj(
              "must" -> Json.obj("match_all" -> Json.obj()),
              "filter" -> JsArray(
                Seq(
                  filtreTypeRecruteur(query.typeRecruteur),
                  Some(filtreMetiersEvalues),
                  Some(filtreRechercheMetier),
                  Some(filtreNumeroTelephone),
                  Some(filtreCommune),
                  query.coordonnees.map(c => filtreLocalisation(c))
                ).flatten
              )
            )
          ),
          "functions" -> JsArray(
            Seq(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(recherche_metiers_evalues -> true)),
                        Json.obj("terms" -> Json.obj(metiers_evalues -> metiers)),
                      ))
                  )
                ),
                "weight" -> 3
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(recherche_autres_metiers -> true)),
                        Json.obj("terms" -> Json.obj(metiers_recherches -> metiers)),
                      ))
                  )
                ),
                "weight" -> 2
              )
            )
          ),
          "score_mode" -> "sum",
          "min_score" -> 2
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj("_score" -> Json.obj("order" -> "desc")),
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          keysetPagination.score.map(JsNumber(_)),
          Some(JsNumber(keysetPagination.dateInscription)),
          keysetPagination.candidatId.map(c => JsString(c.value))
        ).flatten)
      )
    ).getOrElse(queryJson)
  }

  def buildQueryRechercherCandidatsParMetier(query: RechercherCandidatsQuery,
                                             codeROME: CodeROME,
                                             metiersSecteurSansMetierChoisi: List[CodeROME]): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "function_score" -> Json.obj(
          "query" -> Json.obj(
            "bool" -> Json.obj(
              "must" -> Json.obj("match_all" -> Json.obj()),
              "filter" -> JsArray(
                Seq(
                  filtreTypeRecruteur(query.typeRecruteur),
                  Some(filtreMetiersEvalues),
                  Some(filtreRechercheMetier),
                  Some(filtreNumeroTelephone),
                  Some(filtreCommune),
                  query.coordonnees.map(c => filtreLocalisation(c))
                ).flatten
              )
            )
          ),
          "functions" -> JsArray(
            Seq(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(recherche_metiers_evalues -> true)),
                        Json.obj("term" -> Json.obj(metiers_evalues -> codeROME)),
                      ))
                  )
                ),
                "weight" -> 6
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(recherche_autres_metiers -> true)),
                        Json.obj("term" -> Json.obj(metiers_recherches -> codeROME)),
                        Json.obj("terms" -> Json.obj(metiers_evalues -> metiersSecteurSansMetierChoisi)),
                      ))
                  )
                ),
                "weight" -> 3
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(recherche_autres_metiers -> true)),
                        Json.obj("term" -> Json.obj(metiers_recherches -> codeROME))
                      ))
                  )
                ),
                "weight" -> 2
              )
            )
          ),
          "score_mode" -> "sum",
          "min_score" -> 2
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj("_score" -> Json.obj("order" -> "desc")),
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          keysetPagination.score.map(JsNumber(_)),
          Some(JsNumber(keysetPagination.dateInscription)),
          keysetPagination.candidatId.map(c => JsString(c.value))
        ).flatten)
      )
    ).getOrElse(queryJson)
  }

  def buildQueryCandidatPourConseiller(query: CandidatsPourConseillerQuery): JsObject = {
    val queryJson = Json.obj(
      "size" -> (query.nbCandidatsParPage * query.nbPagesACharger),
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "must" -> Json.obj(
            "match_all" -> Json.obj()
          )
        )
      ),
      "sort" -> JsArray(
        Seq(
          Json.obj(date_inscription -> Json.obj("order" -> "desc")),
          Json.obj(candidat_id -> "asc")
        )
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> JsArray(Seq(
          JsNumber(keysetPagination.dateInscription),
          JsString(keysetPagination.candidatId.value)
        ))
      )
    ).getOrElse(queryJson)
  }

  private def filtreTypeRecruteur(typeRecruteur: TypeRecruteur): Option[JsObject] = typeRecruteur match {
    case TypeRecruteur.AGENCE_INTERIM =>
      Some(Json.obj("term" -> Json.obj(contacte_par_agence_interim -> true)))
    case TypeRecruteur.ORGANISME_FORMATION =>
      Some(Json.obj("term" -> Json.obj(contacte_par_organisme_formation -> true)))
    case _ => None
  }

  private def filtreMetiersEvalues: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc['$metiers_evalues'].values.length > 0"
    ))

  private def filtreNumeroTelephone: JsObject =
    Json.obj("exists" -> Json.obj("field" -> numero_telephone))

  private def filtreCommune: JsObject =
    Json.obj("exists" -> Json.obj("field" -> commune))

  private def filtreRechercheMetier: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc.$recherche_metiers_evalues.value == true || doc.$recherche_autres_metiers.value == true"
    ))

  private def filtreLocalisation(coordonnees: Coordonnees): JsObject =
    Json.obj("geo_shape" -> Json.obj(
      mobilite -> Json.obj(
        "shape" -> Json.obj(
          "coordinates" -> JsArray(Seq(JsNumber(coordonnees.longitude), JsNumber(coordonnees.latitude))),
            "type" -> "point"
        ),
        "relation" -> "contains"
      )
    ))
}
