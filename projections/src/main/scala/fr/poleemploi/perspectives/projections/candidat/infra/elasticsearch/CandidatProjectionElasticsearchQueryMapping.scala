package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.LocalDate

import fr.poleemploi.perspectives.candidat.{ExperienceProfessionnelle, Formation, LocalisationRecherche}
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.recruteur.TypeRecruteur
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatProjectionElasticsearchQueryMapping(referentielMetier: ReferentielMetier) {

  import CandidatProjectionElasticsearchMapping._

  def buildCandidatSaisieCriteresRechercheQueryResult(document: CandidatSaisieCriteresRechercheDocument): Future[CandidatSaisieCriteresRechercheQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides.map(_.metier)).map(metiersValides =>
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
        localisationRecherche = buildLocalisationRecherche(document.criteresRecherche),
        tempsTravail = document.criteresRecherche.tempsTravail
      ))

  def buildCandidatSaisieDisponibilitesQueryResult(document: CandidatSaisieDisponibilitesDocument): CandidatSaisieDisponibilitesQueryResult =
    CandidatSaisieDisponibilitesQueryResult(
      candidatId = document.candidatId,
      candidatEnRecherche = document.contactRecruteur.getOrElse(false) || document.contactFormation.getOrElse(false),
      dateProchaineDisponibilite = document.dateProchaineDisponibilite,
      emploiTrouveGracePerspectives = document.emploiTrouveGracePerspectives.getOrElse(false)
    )

  def buildCandidatLocalisationQueryResult(document: CandidatLocalisationDocument): CandidatLocalisationQueryResult =
    CandidatLocalisationQueryResult(
      commune = document.commune,
      codePostal = document.codePostal,
      latitude = document.latitude,
      longitude = document.longitude
    )

  def buildMetiersValidesQueryResult(documents: Set[MetierValideDocument]): Future[CandidatMetiersValidesQueryResult] =
    referentielMetier.metiersParCodesROME(documents.map(_.metier))
      .map(metiersValides => CandidatMetiersValidesQueryResult(metiersValides))

  def buildCandidatPourRechercheOffreQueryResult(document: CandidatPourRechercheOffreDocument): Future[CandidatPourRechercheOffreQueryResult] =
    referentielMetier.metiersParCodesROME(document.metiersValides.map(_.metier)).map(metiersValides =>
      CandidatPourRechercheOffreQueryResult(
        metiersValides = metiersValides,
        localisationRecherche = buildLocalisationRecherche(document.criteresRecherche),
        cv = document.cvId.isDefined
      )
    )

  def buildCandidatsRechercheDto(documents: Seq[CandidatPourRecruteurDocument]): Future[List[CandidatPourRecruteurDto]] =
    for {
      metiersValides <- referentielMetier.metiersParCodesROME(documents.flatMap(_.metiersValides.map(_.metier)).toSet)
      metiersRecherches <- referentielMetier.metiersRechercheParCodeROME(documents.flatMap(_.metiersRecherches).toSet)
    } yield {
      val mapMetiersValides = metiersValides.groupBy(_.codeROME)
      val mapMetiersRecherches = metiersRecherches.groupBy(_.codeROME)
      documents.map(d => CandidatPourRecruteurDto(
        candidatId = d.candidatId,
        nom = d.nom,
        prenom = d.prenom,
        email = d.email,
        metiersValides = d.metiersValides.map(m =>
          MetierValideDTO(
            metier = mapMetiersValides.get(m.metier).head.head,
            habiletes = m.habiletes,
            departement = m.departement,
            isDHAE = m.isDHAE
          )
        ),
        metiersValidesRecherches = d.metiersValidesRecherches.map(c => mapMetiersValides.get(c).head.head),
        metiersRecherches = d.metiersRecherches.map(c => mapMetiersRecherches.get(c).head.head),
        numeroTelephone = d.numeroTelephone,
        rayonRecherche = d.rayonRecherche.map(buildRayonRecherche),
        tempsTravailRecherche = d.tempsTravailRecherche,
        commune = d.communeRecherche,
        codePostal = d.codePostalRecherche,
        cvId = d.cvId,
        cvTypeMedia = d.cvTypeMedia,
        centresInteret = d.centresInteret.sortBy(_.value),
        langues = d.langues,
        permis = d.permis.sortBy(_.code),
        savoirEtre = d.savoirEtre.sortBy(_.value),
        savoirFaire = d.savoirFaire
          .filter(_.niveau.isDefined)
          .sortWith((s1, s2) =>
            (s1.niveau, s2.niveau) match {
              case (None, None) => s1.label < s2.label
              case (Some(_), None) => true
              case (None, Some(_)) => false
              case (Some(n1), Some(n2)) => n1.value > n2.value || (n1.value == n2.value && s1.label < s2.label)
            }
        ),
        formations = d.formations.map(buildFormation).sortWith((f1, f2) => f1.anneeFin > f2.anneeFin),
        experiencesProfessionnelles = d.experiencesProfessionnelles.map(buildExperienceProfessionnelle).sortWith((e1, e2) => e1.dateDebut.isAfter(e2.dateDebut))
      )).toList
    }

  def buildCandidatPourConseillerDto(documents: Seq[CandidatPourConseillerDocument]): Future[List[CandidatPourConseillerDto]] =
    for {
      metiersValides <- referentielMetier.metiersParCodesROME((documents.flatMap(_.metiersValidesRecherches) ++ documents.flatMap(_.metiersValides.map(_.metier))).toSet)
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
          metiersValides = d.metiersValides.map(m =>
            MetierValideDTO(
              metier = mapMetiersValides.get(m.metier).head.head,
              habiletes = m.habiletes,
              departement = m.departement,
              isDHAE = m.isDHAE
            )
          ),
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

  def buildLocalisationRecherche(document: CandidatCriteresRechercheDocument): Option[LocalisationRecherche] =
    for {
      codePostal <- document.codePostal
      commune <- document.commune
      zone <- document.zone
    } yield LocalisationRecherche(
      commune = commune,
      codePostal = codePostal,
      coordonnees = Coordonnees(
        latitude = zone.latitude,
        longitude = zone.longitude
      ),
      rayonRecherche = document.rayon.map(buildRayonRecherche)
    )

  def buildSecteursActivitesAvecCandidatQuery(query: SecteursActivitesAvecCandidatsQuery): JsObject =
    Json.obj(
      "size" -> 0,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "filter" -> Json.arr(
            buildFiltreTypeRecruteur(query.typeRecruteur),
            buildFiltreMetiersValides,
            buildFiltreRechercheMetier,
            buildFiltreNumeroTelephone,
            buildFiltreCommuneRecherche
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
      val secteursAvecCandidats = secteursActivites.map(s => s.copy(
        metiers = s.metiers.filter(m => buckets.exists(b => b.key == m.codeROME.value || b.key.startsWith(m.codeROME.value)))
      )).filter(s => s.metiers.nonEmpty)

      SecteursActivitesAvecCandidatsQueryResult(secteursAvecCandidats)
    }

  def buildRechercheCandidatsQuery(query: RechercheCandidatsQuery): JsObject = {
    def buildFiltresRechercheCandidatQuery(query: RechercheCandidatsQuery): JsArray =
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

    def buildQueryParLocalisation(query: RechercheCandidatsQuery): JsObject =
      Json.obj(
        "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
        "query" -> Json.obj(
          "bool" -> Json.obj(
            "filter" -> buildFiltresRechercheCandidatQuery(query)
          )
        ),
        "sort" -> Json.arr(
          Json.obj(date_inscription -> "desc"),
          Json.obj(candidat_id -> "desc")
        )
      )

    def buildQueryParSecteur(query: RechercheCandidatsQuery,
                             codeSecteurActivite: CodeSecteurActivite): JsObject =
      Json.obj(
        "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
        "query" -> Json.obj(
          "function_score" -> Json.obj(
            "query" -> Json.obj(
              "bool" -> Json.obj(
                "must" -> Json.obj("match_all" -> Json.obj()),
                "filter" -> buildFiltresRechercheCandidatQuery(query)
              )
            ),
            "functions" -> Json.arr(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" -> Json.arr(
                      Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeSecteurActivite))
                    )
                  )
                ),
                "weight" -> 3
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" -> Json.arr(
                      Json.obj("prefix" -> Json.obj(metiers_recherche -> codeSecteurActivite)),
                    )
                  )
                ),
                "weight" -> 2
              )
            ),
            "score_mode" -> "sum",
            "min_score" -> 2
          )
        ),
        "sort" -> Json.arr(
          Json.obj("_score" -> "desc"),
          Json.obj(date_inscription -> "desc"),
          Json.obj(candidat_id -> "desc")
        )
      )

    // FIXME : on passe un codeRome mais ca peut aussi etre un code domaine!
    def buildQueryParMetier(query: RechercheCandidatsQuery,
                            codeROME: CodeROME): JsObject =
      Json.obj(
        "size" -> query.nbCandidatsParPage * query.nbPagesACharger,
        "query" -> Json.obj(
          "function_score" -> Json.obj(
            "query" -> Json.obj(
              "bool" -> Json.obj(
                "must" -> Json.obj("match_all" -> Json.obj()),
                "filter" -> buildFiltresRechercheCandidatQuery(query)
              )
            ),
            "functions" -> Json.arr(
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" -> Json.arr(
                      Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME))
                    ))
                ),
                "weight" -> 6
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must_not" -> Json.arr(
                      Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME))
                    ),
                    "must" -> Json.arr(
                      Json.obj("term" -> Json.obj(metiers_recherche -> codeROME)),
                      Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> codeROME.codeSecteurActivite)),
                    )
                  )
                ),
                "weight" -> 3
              ),
              Json.obj(
                "filter" -> Json.obj(
                  "bool" -> Json.obj(
                    "must" -> Json.arr(
                      Json.obj("term" -> Json.obj(metiers_recherche -> codeROME))
                    )
                  )
                ),
                "weight" -> 2
              )
            ),
            "score_mode" -> "sum",
            "min_score" -> 2
          )
        ),
        "sort" -> Json.arr(
          Json.obj("_score" -> "desc"),
          Json.obj(date_inscription -> "desc"),
          Json.obj(candidat_id -> "desc")
        )
      )

    val queryJson = query.codeROME.map(c => buildQueryParMetier(query, c))
      .orElse(query.codeSecteurActivite.map(c => buildQueryParSecteur(query, c)))
      .getOrElse(buildQueryParLocalisation(query))

    query.page.map(keysetCandidatPourRecruteur =>
      queryJson ++ Json.obj(
        "search_after" -> KeysetCandidatPourRecruteurDocument(
          score = keysetCandidatPourRecruteur.score,
          dateInscription = keysetCandidatPourRecruteur.dateInscription,
          candidatId = keysetCandidatPourRecruteur.candidatId
        )
      )
    ).getOrElse(queryJson)
  }

  def buildCandidatPourConseillerQuery(query: CandidatsPourConseillerQuery): JsObject = {
    val queryJson = Json.obj(
      "size" -> query.nbCandidatsParPage,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "must" -> JsArray(
            Seq(
              query.codePostal.map(c =>
                Json.obj("term" -> Json.obj("criteres_recherche.code_postal" -> c))
              ).orElse(
                if (query.codesDepartement.nonEmpty)
                  Some(Json.obj("bool" -> Json.obj(
                    "should" -> Json.arr(
                      query.codesDepartement.map(c =>
                        Json.obj("prefix" -> Json.obj("criteres_recherche.code_postal" -> c.value))
                      )
                    )
                  )))
                else None
              ),
              if (query.dateDebut.isDefined || query.dateFin.isDefined)
                Some(Json.obj("bool" -> Json.obj(
                  "should" -> Json.arr(
                    buildFiltreDateInscription(query.dateDebut, query.dateFin),
                    buildFiltreDateDerniereConnexion(query.dateDebut, query.dateFin)
                  )
                )))
              else None,
              query.codeSecteurActivite.map(c =>
                Json.obj("bool" -> Json.obj(
                  "should" -> Json.arr(
                    Json.obj("prefix" -> Json.obj(metiers_valides_recherche -> c)),
                    Json.obj("prefix" -> Json.obj(metiers_recherche -> c))
                  )
                ))
              )
            ).flatten
          )
        )
      ),
      "sort" -> Json.arr(
        Json.obj(date_inscription -> "desc"),
        Json.obj(candidat_id -> "desc")
      )
    )

    query.page.map(keysetPagination =>
      queryJson ++ Json.obj(
        "search_after" -> KeysetCandidatPourConseillerDocument(
          dateInscription = keysetPagination.dateInscription,
          candidatId = keysetPagination.candidatId
        )
      )
    ).getOrElse(queryJson)
  }

  def buildCandidatPourBatchDisponibilitesQuery(query: CandidatsPourBatchDisponibilitesQuery): JsObject =
    Json.obj(
      "size" -> query.candidatIds.size,
      "query" -> Json.obj(
        "bool" -> Json.obj(
          "must" -> JsArray(
            Seq(
              Json.obj("terms" -> Json.obj(candidat_id -> JsArray(query.candidatIds.map(v => JsString(v.value))))),
              Json.obj("bool" -> Json.obj(
                "should" -> Json.arr(
                  Json.obj("term" -> Json.obj(contact_recruteur -> true)),
                  Json.obj("term" -> Json.obj(contact_formation -> true))
                )
              ))
            )
          )
        )
      )
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
      "script" -> s"doc['$metiers_valides.metier'].values.length > 0"
    ))

  private def buildFiltreRechercheMetier: JsObject =
    Json.obj("script" -> Json.obj(
      "script" -> s"doc['$metiers_valides_recherche'].values.length > 0 || doc['$metiers_recherche'].values.length > 0"
    ))

  private def buildFiltreLocalisation(coordonnees: Coordonnees): JsObject =
    Json.obj("geo_shape" -> Json.obj(
      zone_recherche -> Json.obj(
        "shape" -> Json.obj(
          "coordinates" -> Json.arr(coordonnees.longitude, coordonnees.latitude),
          "type" -> "point"
        ),
        "relation" -> "contains"
      )
    ))

  private def buildFiltreDateInscription(dateDebut: Option[LocalDate],
                                         dateFin: Option[LocalDate]): JsObject =
    Json.obj("range" -> Json.obj(
      date_inscription -> Json.obj(
        "gte" -> dateDebut,
        "lte" -> dateFin
      )
    ))

  private def buildFiltreDateDerniereConnexion(dateDebut: Option[LocalDate],
                                               dateFin: Option[LocalDate]): JsObject =
    Json.obj("range" -> Json.obj(
      date_derniere_connexion -> Json.obj(
        "gte" -> dateDebut,
        "lte" -> dateFin
      )
    ))

  def buildRayonRecherche(document: RayonRechercheDocument): RayonRecherche =
    RayonRecherche(
      value = document.value,
      uniteLongueur = document.uniteLongueur
    )

  def buildFormation(document: FormationDocument): Formation =
    Formation(
      anneeFin = document.anneeFin,
      intitule = document.intitule,
      lieu = document.lieu,
      domaine = document.domaine,
      niveau = document.niveau
    )

  def buildExperienceProfessionnelle(docuement: ExperienceProfessionnelleDocument): ExperienceProfessionnelle =
    ExperienceProfessionnelle(
      dateDebut = docuement.dateDebut,
      dateFin = docuement.dateFin,
      enPoste = docuement.enPoste,
      intitule = docuement.intitule,
      nomEntreprise = docuement.nomEntreprise,
      lieu = docuement.lieu,
      description = docuement.description
    )
}
