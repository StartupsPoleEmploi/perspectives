package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.projections.candidat._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME : perf referentiel metier
// FIXME : mapping pagination
// FIXME : requete en passant des sous domaines + labels domaine ou sous secteur
class CandidatProjectionElasticsearchAdapter(wsClient: WSClient,
                                             esConfig: EsConfig,
                                             mapping: CandidatProjectionElasticsearchMapping) extends CandidatProjection with WSAdapter {

  import CandidatProjectionElasticsearchMapping._

  val baseUrl = s"${esConfig.host}:${esConfig.port}"
  val indexName = "candidats"
  val docType = "_doc"

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  private val refreshParam: (String, String) = ("refresh", "true")

  override def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
      .withQueryStringParameters(refreshParam)
      .withHttpHeaders(jsonContentType)
      .post(Json.obj(
        candidat_id -> event.candidatId,
        nom -> event.nom,
        prenom -> event.prenom,
        genre -> event.genre,
        email -> event.email,
        date_inscription -> dateTimeFormatter.format(event.date),
        date_derniere_connexion -> dateTimeFormatter.format(event.date),
        metiers_valides -> JsArray.empty,
        habiletes -> JsArray.empty,
        criteres_recherche -> Json.obj(
          "metiers_valides" -> JsArray.empty,
          "metiers" -> JsArray.empty,
          "domaines_professionnels" -> JsArray.empty
        )
      )).map(_ => ())

  override def onCandidatConnecteEvent(event: CandidatConnecteEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      date_derniere_connexion -> dateTimeFormatter.format(event.date)
    ))

  override def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      nom -> event.nom,
      prenom -> event.prenom,
      genre -> event.genre,
      email -> event.email,
    ))

  override def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      code_postal -> event.adresse.codePostal,
      commune -> event.adresse.libelleCommune,
      latitude -> event.coordonnees.latitude,
      longitude -> event.coordonnees.longitude
    ))

  override def onVisibiliteRecruteurModifieeEvent(event: VisibiliteRecruteurModifieeEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      contact_recruteur -> event.contactRecruteur,
      contact_formation -> event.contactFormation
    ))

  override def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit] =
    updateScript(
      candidatId = event.candidatId,
      source = "ctx._source.criteres_recherche = params.criteres_recherche",
      params = Json.obj(
        criteres_recherche -> Json.obj(
          "metiers_valides" -> event.codesROMEValidesRecherches,
          "metiers" -> event.codesROMERecherches,
          "domaines_professionnels" -> event.codesDomaineProfessionnelRecherches,
          "code_postal" -> event.localisationRecherche.codePostal,
          "commune" -> event.localisationRecherche.commune,
          "rayon" -> event.localisationRecherche.rayonRecherche.map(r => RayonRechercheDocument(
            value = r.value,
            uniteLongueur = r.uniteLongueur
          )),
          "zone" -> ZoneDocument( // FIXME : mapping
            typeMobilite = event.localisationRecherche.rayonRecherche.map(_ => "circle").getOrElse("point"),
            latitude = event.localisationRecherche.coordonnees.latitude,
            longitude = event.localisationRecherche.coordonnees.longitude,
            radius = event.localisationRecherche.rayonRecherche.map(r => (r.value * 1.2).toString) // 20% de marge sur les rayons de recherche pour ne pas louper les candidats à quelques kilomètres près
          )
        )
      ))

  override def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      numero_telephone -> event.numeroTelephone,
    ))

  override def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      statut_demandeur_emploi -> event.statutDemandeurEmploi,
    ))

  override def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      cv_id -> event.cvId,
      cv_type_media -> event.typeMedia,
    ))

  override def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      cv_id -> event.cvId,
      cv_type_media -> event.typeMedia,
    ))

  override def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit] =
    for {
      candidat <- wsClient
        .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
        .withQueryStringParameters(refreshParam, ("_source", s"$metiers_valides,$habiletes"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => (Json.parse(r.body) \ "_source").as[CandidatMetiersValidesDocument])
      _ <- update(event.candidatId, Json.obj(
        metiers_valides -> (candidat.metiersValides + event.codeROME),
        habiletes -> (event.habiletes ++ candidat.habiletes)
      ))
    } yield ()

  override def onRepriseEmploiDeclareeParConseillerEvent(event: RepriseEmploiDeclareeParConseillerEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      contact_recruteur -> false,
      contact_formation -> false
    ))

  override def saisieCriteresRecherche(query: CandidatSaisieCriteresRechercheQuery): Future[CandidatSaisieCriteresRechercheQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .get()
      .flatMap(filtreStatutReponse(_))
      .flatMap(r => mapping.buildCandidatSaisieCriteresRechercheQueryResult((Json.parse(r.body) \ "_source").as[CandidatSaisieCriteresRechercheDocument]))

  override def localisation(query: CandidatLocalisationQuery): Future[CandidatLocalisationQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$commune,$code_postal,$latitude,$longitude")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildCandidatLocalisationQueryResult((Json.parse(r.body) \ "_source").as[CandidatLocalisationDocument]))

  override def metiersValides(query: CandidatMetiersValidesQuery): Future[CandidatMetiersValidesQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$metiers_valides,$habiletes")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .flatMap(r => mapping.buildMetiersValidesQueryResult((Json.parse(r.body) \ "_source").as[CandidatMetiersValidesDocument]))

  override def depotCV(query: CandidatDepotCVQuery): Future[CandidatDepotCVQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$candidat_id,$nom,$prenom,$cv_id,$cv_type_media")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildCandidatDepotCVQueryResult((Json.parse(r.body) \ "_source").as[CandidatDepotCVDocument]))

  override def rechercheOffre(query: CandidatPourRechercheOffreQuery): Future[CandidatPourRechercheOffreQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$candidat_id,$metiers_valides,$criteres_recherche,$cv_id")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .flatMap(r => mapping.buildCandidatPourRechercheOffreQueryResult((Json.parse(r.body) \ "_source").as[CandidatPourRechercheOffreDocument]))

  override def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$contact_recruteur,$contact_formation")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildContactRecruteurQueryResult((Json.parse(r.body) \ "_source").as[CandidatContactRecruteurDocument]))

  override def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildCandidatPourConseillerQuery(query))
      .flatMap { response =>
        val hits = (Json.parse(response.body) \ "hits" \ "hits").as[JsArray]
        val candidats = (hits \\ "_source").take(query.nbCandidatsParPage).map(_.as[CandidatPourConseillerDocument])
        val pages = (hits \\ "sort").zipWithIndex
          .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
          .map(v => KeysetCandidatsPourConseiller(
            dateInscription = if (v._2 == 0) (v._1 \ 0).as[Long] + 1L else (v._1 \ 0).as[Long],
            candidatId = (v._1 \ 1).as[CandidatId]
          )).toList

        mapping.buildCandidatPourConseillerDto(candidats).map(dtos =>
          CandidatsPourConseillerQueryResult(
            candidats = dtos,
            pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
            pageSuivante = pages.reverse.headOption
          )
        )
      }

  override def rechercherCandidats(query: RechercheCandidatsQuery): Future[RechercheCandidatQueryResult] =
    query.codeROME.map(codeROME => rechercherCandidatsParMetier(query = query, codeROME = codeROME))
      .orElse(query.codeSecteurActivite.map(codeSecteurActivite => rechercherCandidatsParSecteur(query = query, codeSecteurActivite = codeSecteurActivite)))
      .getOrElse {
        wsClient
          .url(s"$baseUrl/$indexName/_search")
          .withHttpHeaders(jsonContentType)
          .post(mapping.buildRechercheCandidatsParLocalisationQuery(query))
          .flatMap { response =>
            val json = Json.parse(response.body)
            val hits = (json \ "hits" \ "hits").as[JsArray]
            val candidats = (hits \\ "_source").take(query.nbCandidatsParPage).map(_.as[CandidatRechercheRecruteurDocument])
            val pages = (hits \\ "sort").zipWithIndex
              .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
              .map(v => KeysetRechercherCandidats(
                score = None,
                dateInscription = if (v._2 == 0) (v._1 \ 0).as[Long] + 1L else (v._1 \ 0).as[Long],
                candidatId = Some((v._1 \ 1).as[CandidatId])
              )).toList
            val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

            mapping.buildCandidatsRechercheDto(candidats).map(dtos =>
              RechercheCandidatParLocalisationQueryResult(
                candidats = dtos,
                nbCandidats = candidats.size,
                nbCandidatsTotal = nbCandidatsTotal,
                pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
                pageSuivante = pages.reverse.headOption
              )
            )
          }
      }

  private def rechercherCandidatsParSecteur(query: RechercheCandidatsQuery,
                                            codeSecteurActivite: CodeSecteurActivite): Future[RechercheCandidatQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildRechercheCandidatsParSecteurQuery(query, codeSecteurActivite))
      .flatMap { response =>
        val json = Json.parse(response.body)
        val hits = (json \ "hits" \ "hits").as[JsArray]
        val pages = buildKeysetRechercheCandidats(query, hits)
        val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

        val candidats = hits.value.take(query.nbCandidatsParPage).toList
        val candidatsEvaluesSurSecteur =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] > 2)
            .map(js => (js \ "_source").as[CandidatRechercheRecruteurDocument])
        val candidatsInteressesParAutreSecteur =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] == 2)
            .map(js => (js \ "_source").as[CandidatRechercheRecruteurDocument])

        for {
          candidatsEvaluesSurSecteur <- mapping.buildCandidatsRechercheDto(candidatsEvaluesSurSecteur)
          candidatsInteressesParAutreSecteur <- mapping.buildCandidatsRechercheDto(candidatsInteressesParAutreSecteur)
        } yield {
          RechercheCandidatParSecteurQueryResult(
            candidatsEvaluesSurSecteur = candidatsEvaluesSurSecteur,
            candidatsInteressesParAutreSecteur = candidatsInteressesParAutreSecteur,
            nbCandidats = candidats.size,
            nbCandidatsTotal = nbCandidatsTotal,
            pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
            pageSuivante = pages.reverse.headOption
          )
        }
      }

  private def rechercherCandidatsParMetier(query: RechercheCandidatsQuery,
                                           codeROME: CodeROME): Future[RechercheCandidatQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildRechercheCandidatsParMetierQuery(query, codeROME))
      .flatMap { response =>
        val json = Json.parse(response.body)
        val hits = (json \ "hits" \ "hits").as[JsArray]
        val pages = buildKeysetRechercheCandidats(query, hits)
        val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

        val candidats = hits.value.take(query.nbCandidatsParPage).toList
        val candidatsEvaluesSurMetier =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] >= 6)
            .map(js => (js \ "_source").as[CandidatRechercheRecruteurDocument])
        val candidatsInteressesParMetier =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] >= 2 && (jsValue \ "_score").as[Int] < 6)
            .map(js => (js \ "_source").as[CandidatRechercheRecruteurDocument])

        for {
          candidatsEvaluesSurMetier <- mapping.buildCandidatsRechercheDto(candidatsEvaluesSurMetier)
          candidatsInteressesParMetier <- mapping.buildCandidatsRechercheDto(candidatsInteressesParMetier)
        } yield {
          RechercheCandidatParMetierQueryResult(
            candidatsEvaluesSurMetier = candidatsEvaluesSurMetier,
            candidatsInteressesParMetier = candidatsInteressesParMetier,
            nbCandidats = candidats.size,
            nbCandidatsTotal = nbCandidatsTotal,
            pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
            pageSuivante = pages.reverse.headOption
          )
        }
      }


  private def buildKeysetRechercheCandidats(query: RechercheCandidatsQuery,
                                            hits: JsArray): List[KeysetRechercherCandidats] =
    (hits \\ "sort").zipWithIndex
      .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
      .map(v => KeysetRechercherCandidats(
        score = Some((v._1 \ 0).as[Int]),
        dateInscription = if (v._2 == 0) (v._1 \ 1).as[Long] + 1L else (v._1 \ 1).as[Long],
        candidatId = Some((v._1 \ 2).as[CandidatId])
      )).toList

  private def update(candidatId: CandidatId, json: JsObject): Future[Unit] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${candidatId.value}/_update")
      .withQueryStringParameters(refreshParam)
      .withHttpHeaders(jsonContentType)
      .post(
        Json.obj("doc" -> json)
      ).flatMap(filtreStatutReponse(_))
      .map(_ => ())

  private def updateScript(candidatId: CandidatId, source: String, params: JsObject): Future[Unit] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${candidatId.value}/_update")
      .withQueryStringParameters(refreshParam)
      .withHttpHeaders(jsonContentType)
      .post(
        Json.obj("script" -> Json.obj(
          "source" -> source,
          "lang" -> "painless",
          "params" -> params
        ))
      ).flatMap(filtreStatutReponse(_))
      .map(_ => ())
}
