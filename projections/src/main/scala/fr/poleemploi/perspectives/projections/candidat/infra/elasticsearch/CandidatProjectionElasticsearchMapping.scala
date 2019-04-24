package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import fr.poleemploi.perspectives.candidat.LocalisationRecherche
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME : séparer en mapping Update et en mapping QueryResult
class CandidatProjectionElasticsearchMapping(referentielMetier: ReferentielMetier) {

  import CandidatProjectionElasticsearchMapping._

  def buildCandidatSaisieCriteresRechercheQueryResult(document: CandidatSaisieCriteresRechercheDocument): Future[CandidatSaisieCriteresRechercheQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides).map(metiersValides =>
      CandidatSaisieCriteresRechercheQueryResult(
        candidatId = document.candidatId,
        contactRecruteur = document.contactRecruteur,
        contactFormation = document.contactFormation,
        numeroTelephone = document.numeroTelephone,
        commune = document.commune,
        codePostal = document.codePostal,
        latitude = document.latitude,
        longitude = document.longitude,
        metiersValides = metiersValides,
        metiersValidesRecherches = document.criteresRecherche.metiersValides,
        metiersRecherches = document.criteresRecherche.metiers,
        domainesProfessionnelsRecherches = document.criteresRecherche.domainesProfessionels,
        codePostalRecherche = document.criteresRecherche.codePostal,
        communeRecherche = document.criteresRecherche.commune,
        latitudeRecherche = document.criteresRecherche.zone.map(_.latitude),
        longitudeRecherche = document.criteresRecherche.zone.map(_.longitude),
        rayonRecherche = document.criteresRecherche.rayon.map(buildRayonRecherche)
      ))

  def buildCandidatLocalisationQueryResult(document: CandidatLocalisationDocument): CandidatLocalisationQueryResult =
    CandidatLocalisationQueryResult(
      commune = document.commune,
      codePostal = document.codePostal,
      latitude = document.latitude,
      longitude = document.longitude
    )

  def buildMetiersValidesQueryResult(document: CandidatMetiersValidesDocument): Future[CandidatMetiersValidesQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides)
      .map(metiersValides => CandidatMetiersValidesQueryResult(metiersValides))

  def buildCandidatDepotCVQueryResult(document: CandidatDepotCVDocument): CandidatDepotCVQueryResult =
    CandidatDepotCVQueryResult(
      candidatId = document.candidatId,
      cvId = document.cvId,
      cvTypeMedia = document.cvTypeMedia
    )

  def buildCandidatPourRechercheOffreQueryResult(document: CandidatPourRechercheOffreDocument): Future[CandidatPourRechercheOffreQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides).map(metiersValides =>
      CandidatPourRechercheOffreQueryResult(
        metiersValides = metiersValides,
        localisationRecherche =
          for {
            codePostal <- document.criteresRecherche.codePostal
            commune <- document.criteresRecherche.commune
          } yield LocalisationRecherche(
            commune = commune,
            codePostal = codePostal,
            coordonnees = document.criteresRecherche.zone.map(z => Coordonnees(
              latitude = z.latitude,
              longitude = z.longitude
            )).get,
            rayonRecherche = document.criteresRecherche.rayon.map(buildRayonRecherche)
          ),
        cv = document.cvId.isDefined
      )
    )

  def buildCandidatsRechercheDto(documents: Seq[CandidatRechercheRecruteurDocument]): Future[List[CandidatRechercheRecruteurDto]] =
    for {
      metiersValides <- referentielMetier.metiersParCodesROME(documents.flatMap(_.metiersValides).toSet)
      metiersRecherches <- referentielMetier.metiersRechercheParCodeROME(documents.flatMap(_.metiersRecherches).toSet)
    } yield {
      val mapMetiersValides = metiersValides.groupBy(_.codeROME)
      val mapMetiersRecherches = metiersRecherches.groupBy(_.codeROME)
      documents.map(d => CandidatRechercheRecruteurDto(
        candidatId = d.candidatId,
        nom = d.nom,
        prenom = d.prenom,
        email = d.email,
        metiersValides = d.metiersValides.map(c => mapMetiersValides.get(c).head.head),
        habiletes = d.habiletes,
        metiersValidesRecherches = d.metiersValidesRecherches.map(c => mapMetiersValides.get(c).head.head),
        metiersRecherches = d.metiersRecherches.map(c => mapMetiersRecherches.get(c).head.head),
        numeroTelephone = d.numeroTelephone,
        rayonRecherche = d.rayonRecherche.map(buildRayonRecherche),
        commune = d.communeRecherche,
        cvId = d.cvId,
        cvTypeMedia = d.cvTypeMedia
      )).toList
    }

  def buildCandidatPourConseillerDto(documents: Seq[CandidatPourConseillerDocument]): Future[List[CandidatPourConseillerDto]] =
    for {
      metiersValides <- referentielMetier.metiersParCodesROME((documents.flatMap(_.metiersValidesRecherches) ++ documents.flatMap(_.metiersValides)).toSet)
      metiersRecherches <- referentielMetier.metiersRechercheParCodeROME(documents.flatMap(_.metiersRecherches).toSet)
    } yield {
      val mapMetiersValides = metiersValides.groupBy(_.codeROME)
      val mapMetiersRecherches = metiersRecherches.groupBy(_.codeROME)
      documents.map(d =>
        CandidatPourConseillerDto(
          candidatId = d.candidatId,
          nom = d.nom,
          prenom = d.prenom,
          email = d.email,
          statutDemandeurEmploi = d.statutDemandeurEmploi,
          metiersValides = d.metiersValides.map(c => mapMetiersValides.get(c).head.head),
          metiersValidesRecherches = d.metiersValidesRecherches.map(c => mapMetiersValides.get(c).head.head),
          metiersRecherches = d.metiersRecherches.map(c => mapMetiersRecherches.get(c).head.head),
          contactRecruteur = d.contactRecruteur,
          contactFormation = d.contactFormation,
          communeRecherche = d.communeRecherche,
          codePostalRecherche = d.codePostalRecherche,
          rayonRecherche = d.rayonRecherche.map(buildRayonRecherche),
          numeroTelephone = d.numeroTelephone,
          dateInscription = d.dateInscription,
          dateDerniereConnexion = d.dateDerniereConnexion
        )).toList
    }

  def buildContactRecruteurQueryResult(document: CandidatContactRecruteurDocument): CandidatContactRecruteurQueryResult =
    CandidatContactRecruteurQueryResult(
      contactRecruteur = document.contactRecruteur,
      contactFormation = document.contactFormation
    )

  def buildZoneDocument(coordonnees: Coordonnees, rayonRecherche: Option[RayonRecherche]): ZoneDocument =
    ZoneDocument(
      typeMobilite = rayonRecherche.map(_ => "circle").getOrElse("point"),
      latitude = coordonnees.latitude,
      longitude = coordonnees.longitude,
      radius = rayonRecherche
        .map(r => r.uniteLongueur match {
          case UniteLongueur.KM => s"${r.value * 1.2}km"
          case u@_ => throw new IllegalArgumentException(s"Unite de longueur non gérée ${u.value}")
        })
    )

  def buildSecteursActivitesAvecCandidatQuery(query: SecteursActivitesAvecCandidatsQuery): JsObject =
    Json.obj(
      "size" -> 0,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "filter" -> JsArray(
            Seq(
              Some(buildFiltreTypeRecruteur(query.typeRecruteur)),
              Some(buildFiltreMetiersValides),
              Some(buildFiltreRechercheMetier),
              Some(buildFiltreNumeroTelephone),
              Some(buildFiltreCommuneRecherche)
            ).flatten
          )
        )
      ),
      "aggs" -> Json.obj(
        "metiers_valides_recherches" -> Json.obj(
          "terms" -> Json.obj(
            "field" -> "criteres_recherche.metiers_valides",
            "size" -> 50
          )
        ),
        "metiers_recherches" -> Json.obj(
          "terms" -> Json.obj(
            "field" -> "criteres_recherche.metiers",
            "size" -> 50
          )
        )
      )
    )

  def buildSecteursActivitesAvecCandidatQueryResult(json: JsValue): Future[SecteursActivitesAvecCandidatsQueryResult] =
    referentielMetier.secteursActivitesRecherche.map { secteursActivites =>
      val buckets = (json \\ "buckets").flatMap(_.as[List[BucketDocument]])
      val filtered = secteursActivites.map(s => s.copy(
        metiers = s.metiers.filter(m => buckets.exists(b => b.key == m.codeROME.value || b.key.startsWith(m.codeROME.value)))
      ))

      SecteursActivitesAvecCandidatsQueryResult(filtered)
    }

  def buildRechercheCandidatsParLocalisationQuery(query: RechercheCandidatsQuery): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "filter" -> buildFiltresRechercheCandidatQuery(query)
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

  def buildRechercheCandidatsParSecteurQuery(query: RechercheCandidatsQuery,
                                             codeSecteurActivite: CodeSecteurActivite): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "function_score" -> Json.obj(
          "query" -> Json.obj(
            "bool" -> Json.obj(
              "must" -> Json.obj("match_all" -> Json.obj()),
              "filter" -> buildFiltresRechercheCandidatQuery(query)
            )
          ),
          "functions" -> JsArray(
            Seq(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeSecteurActivite))
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
                        Json.obj("prefix" -> Json.obj(metiers_recherche -> codeSecteurActivite)),
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

  // FIXME : on passe un codeRome mais ca peut aussi etre un code domaine!
  def buildRechercheCandidatsParMetierQuery(query: RechercheCandidatsQuery,
                                            codeROME: CodeROME): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
      "query" -> Json.obj(
        "function_score" -> Json.obj(
          "query" -> Json.obj(
            "bool" -> Json.obj(
              "must" -> Json.obj("match_all" -> Json.obj()),
              "filter" -> buildFiltresRechercheCandidatQuery(query)
            )
          ),
          "functions" -> JsArray(
            Seq(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME)),
                      ))
                  )
                ),
                "weight" -> 6
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must_not" ->
                      JsArray(Seq(
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME))
                      )),
                    "must" ->
                      JsArray(Seq(
                        Json.obj("term" -> Json.obj(metiers_recherche -> codeROME)),
                        Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME.codeSecteurActivite)),
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
                        Json.obj("term" -> Json.obj(metiers_recherche -> codeROME))
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

  def buildCandidatPourConseillerQuery(query: CandidatsPourConseillerQuery): JsObject = {
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

  private def buildFiltresRechercheCandidatQuery(query: RechercheCandidatsQuery): JsArray =
    JsArray(
      Seq(
        Some(buildFiltreTypeRecruteur(query.typeRecruteur)),
        Some(buildFiltreMetiersValides),
        Some(buildFiltreRechercheMetier),
        Some(buildFiltreNumeroTelephone),
        Some(buildFiltreCommuneRecherche),
        query.coordonnees.map(c => buildFiltreLocalisation(c))
      ).flatten
    )

  private def buildFiltreTypeRecruteur(typeRecruteur: TypeRecruteur): JsObject = typeRecruteur match {
    case TypeRecruteur.ORGANISME_FORMATION =>
      Json.obj("term" -> Json.obj(contact_formation -> true))
    case _ =>
      Json.obj("term" -> Json.obj(contact_recruteur -> true))
  }

  private def buildFiltreNumeroTelephone: JsObject =
    Json.obj("exists" -> Json.obj("field" -> numero_telephone))

  private def buildFiltreCommuneRecherche: JsObject =
    Json.obj("exists" -> Json.obj("field" -> commune_recherche))

  private def buildFiltreMetiersValides: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc['$metiers_valides'].values.length > 0"
    ))

  private def buildFiltreRechercheMetier: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc['$metiers_valides_recherche'].values.length > 0 || doc['$metiers_recherche'].values.length > 0"
    ))

  private def buildFiltreLocalisation(coordonnees: Coordonnees): JsObject =
    Json.obj("geo_shape" -> Json.obj(
      zone_recherche -> Json.obj(
        "shape" -> Json.obj(
          "coordinates" -> JsArray(Seq(JsNumber(coordonnees.longitude), JsNumber(coordonnees.latitude))),
          "type" -> "point"
        ),
        "relation" -> "contains"
      )
    ))

  private def buildRayonRecherche(document: RayonRechercheDocument): RayonRecherche =
    RayonRecherche(value = document.value, uniteLongueur = document.uniteLongueur)
}

object CandidatProjectionElasticsearchMapping {

  val candidat_id = "candidat_id"
  val nom = "nom"
  val prenom = "prenom"
  val genre = "genre"
  val email = "email"
  val numero_telephone = "numero_telephone"
  val statut_demandeur_emploi = "statut_demandeur_emploi"
  val code_postal = "code_postal"
  val commune = "commune"
  val latitude = "latitude"
  val longitude = "longitude"
  val metiers_valides = "metiers_valides"
  val habiletes = "habiletes"
  val contact_recruteur = "contact_recruteur"
  val contact_formation = "contact_formation"
  val cv_id = "cv_id"
  val cv_type_media = "cv_type_media"
  val date_inscription = "date_inscription"
  val date_derniere_connexion = "date_derniere_connexion"

  val criteres_recherche = "criteres_recherche"
  val metiers_valides_recherche = "criteres_recherche.metiers_valides"
  val metiers_recherche = "criteres_recherche.metiers"
  val domaines_professionnels_recherche = "criteres_recherche.domaines_professionnels"
  val code_postal_recherche = "criteres_recherche.code_postal"
  val commune_recherche = "criteres_recherche.commune"
  val rayon_recherche = "criteres_recherche.rayon"
  val zone_recherche = "criteres_recherche.zone"
}
