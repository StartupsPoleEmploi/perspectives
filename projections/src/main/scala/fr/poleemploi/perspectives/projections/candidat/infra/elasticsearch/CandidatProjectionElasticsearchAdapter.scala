package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.metier.domain.ReferentielMetier
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.rechercheCandidat.domain.RechercheCandidatService
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CandidatProjectionElasticsearchAdapter(wsClient: WSClient,
                                             esConfig: EsConfig,
                                             referentielMetier: ReferentielMetier,
                                             rechercheCandidatService: RechercheCandidatService) extends CandidatProjection with WSAdapter {

  import CandidatProjectionElasticsearchEsMapping._

  val baseUrl = s"${esConfig.host}:${esConfig.port}"
  val indexName = "candidats"
  val docType = "_doc"

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  private val jsonHttpHeader: (String, String) = ("Content-Type", "application/json")
  private val refreshParam: (String, String) = ("refresh", "true")

  def onCandidatInscritEvent(event: CandidatInscritEvent): Future[Unit] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
      .withQueryStringParameters(refreshParam)
      .withHttpHeaders(jsonHttpHeader)
      .post(Json.obj(
        candidat_id -> event.candidatId,
        nom -> event.nom,
        prenom -> event.prenom,
        genre -> event.genre,
        email -> event.email,
        date_inscription -> dateTimeFormatter.format(event.date),
        date_derniere_connexion -> dateTimeFormatter.format(event.date),
        metiers_recherches -> JsArray.empty,
        metiers_evalues -> JsArray.empty,
        habiletes -> JsArray.empty
      )).map(_ => ())

  def onCandidatConnecteEvent(event: CandidatConnecteEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      date_derniere_connexion -> dateTimeFormatter.format(event.date)
    ))

  def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      nom -> event.nom,
      prenom -> event.prenom,
      genre -> event.genre,
      email -> event.email,
    ))

  def onAdresseModifieeEvent(event: AdresseModifieeEvent): Future[Unit] =
    for {
      rayonRecherche <- wsClient
        .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
        .withQueryStringParameters(refreshParam, ("_source", s"$rayon_recherche"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => (Json.parse(r.body) \ "_source" \ s"$rayon_recherche").asOpt[Int])
      _ <- update(event.candidatId, Json.obj(
        code_postal -> event.adresse.codePostal,
        commune -> event.adresse.libelleCommune,
        mobilite -> event.coordonnees.map(coordonnees =>
          rayonRecherche.map(rayonRecherche =>
            MobiliteDocument(
              typeMobilite = "circle",
              latitude = coordonnees.latitude,
              longitude = coordonnees.longitude,
              radius = Some((rayonRecherche * 1.2).toString) // 20% de marge sur les rayons de recherche pour ne pas louper les candidats à quelques kilomètres près
            )
          ).getOrElse(
            MobiliteDocument(
              typeMobilite = "point",
              latitude = coordonnees.latitude,
              longitude = coordonnees.longitude,
              radius = None
            )
          )
        )
      ))
    } yield ()

  def onCriteresRechercheModifiesEvent(event: CriteresRechercheModifiesEvent): Future[Unit] =
    for {
      optMobilite <- wsClient
        .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
        .withQueryStringParameters(refreshParam, ("_source", s"$mobilite"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => (Json.parse(r.body) \ "_source" \ s"$mobilite").asOpt[MobiliteDocument])
      _ <- update(event.candidatId, Json.obj(
        recherche_metiers_evalues -> event.rechercheMetierEvalue,
        recherche_autres_metiers -> event.rechercheAutreMetier,
        metiers_recherches -> event.metiersRecherches,
        contacte_par_agence_interim -> event.etreContacteParAgenceInterim,
        contacte_par_organisme_formation -> event.etreContacteParOrganismeFormation,
        rayon_recherche -> event.rayonRecherche,
        mobilite -> optMobilite.map(_.copy(
          typeMobilite = "circle",
          radius = Some((event.rayonRecherche.value * 1.2).toString) // 20% de marge sur les rayons de recherche pour ne pas louper les candidats à quelques kilomètres près
        ))
      ))
    } yield ()


  def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      numero_telephone -> event.numeroTelephone,
    ))

  def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      statut_demandeur_emploi -> event.statutDemandeurEmploi,
    ))

  def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      cv_id -> event.cvId,
      cv_type_media -> event.typeMedia,
    ))

  def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      cv_id -> event.cvId,
      cv_type_media -> event.typeMedia,
    ))

  def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit] =
    for {
      candidat <- wsClient
        .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
        .withQueryStringParameters(refreshParam, ("_source", s"$metiers_evalues,$habiletes"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => (Json.parse(r.body) \ "_source").as[CandidatMetiersEvaluesDocument])
      _ <- update(event.candidatId, Json.obj(
        metiers_evalues -> (event.metier :: candidat.metiersEvalues).distinct,
        habiletes -> (event.habiletes ++ candidat.habiletes).distinct
      ))
    } yield ()

  def onRepriseEmploiDeclareeParConseillerEvent(event: RepriseEmploiDeclareeParConseillerEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      recherche_metiers_evalues -> false,
      recherche_autres_metiers -> false,
      metiers_recherches -> JsArray.empty
    ))

  override def candidatSaisieCriteresRecherche(query: CandidatSaisieCriteresRechercheQuery): Future[CandidatSaisieCriteresRechercheQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .get()
      .flatMap(filtreStatutReponse(_))
      .map { response =>
        val document = (Json.parse(response.body) \ "_source").as[CandidatSaisieCriteresRechercheDocument]
        toCandidatSaisieCriteresRechercheQueryResult(document)
      }

  override def candidatCriteresRecherche(query: CandidatCriteresRechercheQuery): Future[CandidatCriteresRechercheQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .get()
      .flatMap(filtreStatutReponse(_))
      .map { response =>
        val document = (Json.parse(response.body) \ "_source").as[CandidatCriteresRechercheDocument]
        toCandidatCriteresRechercheQueryResult(document)
      }

  override def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurDto] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$contacte_par_organisme_formation,$contacte_par_agence_interim")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(response =>
        (Json.parse(response.body) \ "_source").as[CandidatContactRecruteurDocument].toContactRecruteurDto
      )

  override def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult] = {
    val queryJson = buildQueryCandidatPourConseiller(query)
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonHttpHeader)
      .post(queryJson)
      .map { response =>
        val hits = (Json.parse(response.body) \ "hits" \ "hits").as[JsArray]
        val candidats = (hits \\ "_source").map(_.as[CandidatPourConseillerDocument]).map(toCandidatPourConseillerDto).toList
        val pages = (hits \\ "sort").zipWithIndex
        .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
        .map(v => KeysetCandidatsPourConseiller(
          dateInscription = if (v._2 == 0) (v._1 \ 0).as[Long] + 1L else (v._1 \ 0).as[Long],
          candidatId = (v._1 \ 1).as[CandidatId]
        )).toList

        CandidatsPourConseillerQueryResult(
          candidats = candidats.take(query.nbCandidatsParPage),
          pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
          pageSuivante = pages.reverse.headOption
        )
      }
  }

  override def rechercherCandidats(query: RechercherCandidatsQuery): Future[RechercheCandidatQueryResult] =
    query.codeROME.map(codeROME => rechercherCandidatsParMetier(query = query, codeROME = codeROME))
      .orElse(query.codeSecteurActivite.map(codeSecteurActivite => rechercherCandidatsParSecteur(query = query, codeSecteurActivite = codeSecteurActivite)))
      .getOrElse {
        wsClient
          .url(s"$baseUrl/$indexName/_search")
          .withHttpHeaders(jsonHttpHeader)
          .post(buildQueryRechercherCandidatsParLocalisation(query))
          .map { response =>
            val json = Json.parse(response.body)
            val hits = (json \ "hits" \ "hits").as[JsArray]
            val candidats = (hits \\ "_source").map(_.as[CandidatRechercheDocument]).map(toCandidatRechercheDto).toList
            val pages = (hits \\ "sort").zipWithIndex
              .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
              .map(v => KeysetRechercherCandidats(
                score = None,
                dateInscription = if (v._2 == 0) (v._1 \ 0).as[Long] + 1L else (v._1 \ 0).as[Long],
                candidatId = Some((v._1 \ 1).as[CandidatId])
              )).toList
            val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

            RechercheCandidatParLocalisationQueryResult(
              candidats = candidats.take(query.nbCandidatsParPage),
              nbCandidats = candidats.size,
              nbCandidatsTotal = nbCandidatsTotal,
              pages = query.page.map(k => k :: pages.tail).getOrElse(pages),
              pageSuivante = pages.reverse.headOption
            )
          }
      }

  private def rechercherCandidatsParSecteur(query: RechercherCandidatsQuery,
                                            codeSecteurActivite: CodeSecteurActivite): Future[RechercheCandidatQueryResult] = {
    val metiersSecteur = rechercheCandidatService.secteurActiviteParCode(codeSecteurActivite).metiers.map(_.codeROME)

    val queryjson = buildQueryRechercherCandidatsParSecteur(query, metiersSecteur)
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonHttpHeader)
      .post(queryjson)
      .map { response =>
        val json = Json.parse(response.body)
        val hits = (json \ "hits" \ "hits").as[JsArray]
        val pages = (hits \\ "sort").zipWithIndex
          .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
          .map(v => KeysetRechercherCandidats(
            score = Some((v._1 \ 0).as[Int]),
            dateInscription = if (v._2 == 0) (v._1 \ 1).as[Long] + 1L else (v._1 \ 1).as[Long],
            candidatId = Some((v._1 \ 2).as[CandidatId])
          )).toList
        val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

        val candidats = hits.value.take(query.nbCandidatsParPage).toList
        val candidatsEvaluesSurSecteur =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] > 2)
            .map(js => toCandidatRechercheDto((js \ "_source").as[CandidatRechercheDocument]))
        val candidatsInteressesParAutreSecteur =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] == 2)
            .map(js => toCandidatRechercheDto((js \ "_source").as[CandidatRechercheDocument]))

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

  private def rechercherCandidatsParMetier(query: RechercherCandidatsQuery,
                                           codeROME: CodeROME): Future[RechercheCandidatQueryResult] = {
    val metiersSecteur = rechercheCandidatService.secteurActivitePourCodeROME(codeROME).metiers.map(_.codeROME)
    val metiersSecteurSansMetierChoisi = metiersSecteur.filter(_ != codeROME)

    val queryJson = buildQueryRechercherCandidatsParMetier(query, codeROME, metiersSecteurSansMetierChoisi)
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonHttpHeader)
      .post(queryJson)
      .map { response =>
        val json = Json.parse(response.body)
        val hits = (json \ "hits" \ "hits").as[JsArray]
        val pages = (hits \\ "sort").zipWithIndex
          .filter(v => v._2 == 0 || (v._2 + 1) % query.nbCandidatsParPage == 0)
          .map(v => KeysetRechercherCandidats(
            score = Some((v._1 \ 0).as[Int]),
            dateInscription = if (v._2 == 0) (v._1 \ 1).as[Long] + 1L else (v._1 \ 1).as[Long],
            candidatId = Some((v._1 \ 2).as[CandidatId])
          )).toList
        val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

        val candidats = hits.value.take(query.nbCandidatsParPage).toList
        val candidatsEvaluesSurMetier =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] >= 6)
            .map(js => toCandidatRechercheDto((js \ "_source").as[CandidatRechercheDocument]))
        val candidatsInteressesParMetier =
          candidats
            .filter(jsValue => (jsValue \ "_score").as[Int] >= 2 && (jsValue \ "_score").as[Int] < 6)
            .map(js => toCandidatRechercheDto((js \ "_source").as[CandidatRechercheDocument]))

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

  private def update(candidatId: CandidatId, json: JsObject): Future[Unit] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${candidatId.value}/_update")
      .withQueryStringParameters(refreshParam)
      .withHttpHeaders(jsonHttpHeader)
      .post(
        Json.obj("doc" -> json)
      ).map(_ => ())

  private def toCandidatSaisieCriteresRechercheQueryResult(document: CandidatSaisieCriteresRechercheDocument): CandidatSaisieCriteresRechercheQueryResult =
    CandidatSaisieCriteresRechercheQueryResult(
      candidatId = document.candidatId,
      nom = document.nom,
      prenom = document.prenom,
      rechercheMetierEvalue = document.rechercheMetierEvalue,
      metiersEvalues = document.metiersEvalues.map(referentielMetier.metierParCode),
      rechercheAutreMetier = document.rechercheAutreMetier,
      metiersRecherches = document.metiersRecherches,
      contacteParAgenceInterim = document.contacteParAgenceInterim,
      contacteParOrganismeFormation = document.contacteParOrganismeFormation,
      rayonRecherche = document.rayonRecherche,
      numeroTelephone = document.numeroTelephone,
      cvId = document.cvId,
      cvTypeMedia = document.cvTypeMedia
    )

  private def toCandidatCriteresRechercheQueryResult(document: CandidatCriteresRechercheDocument): CandidatCriteresRechercheQueryResult =
    CandidatCriteresRechercheQueryResult(
      candidatId = document.candidatId,
      rechercheMetiersEvalues = document.rechercheMetiersEvalues,
      metiersEvalues = document.metiersEvalues.map(referentielMetier.metierParCode),
      rechercheAutresMetiers = document.rechercheAutresMetiers,
      metiersRecherches = document.metiersRecherches.map(referentielMetier.metierParCode),
      codePostal = document.codePostal,
      commune = document.commune,
      rayonRecherche = document.rayonRecherche
    )

  private def toCandidatRechercheDto(document: CandidatRechercheDocument): CandidatRechercheDto =
    CandidatRechercheDto(
      candidatId = document.candidatId,
      nom = document.nom,
      prenom = document.prenom,
      email = document.email,
      metiersEvalues = document.metiersEvalues.map(referentielMetier.metierParCode),
      habiletes = document.habiletes,
      metiersRecherches = document.metiersRecherches.flatMap(rechercheCandidatService.metierProposeParCode),
      numeroTelephone = document.numeroTelephone,
      rayonRecherche = document.rayonRecherche,
      commune = document.commune,
      cvId = document.cvId,
      cvTypeMedia = document.cvTypeMedia
    )

  private def toCandidatPourConseillerDto(document: CandidatPourConseillerDocument): CandidatPourConseillerDto =
    CandidatPourConseillerDto(
      candidatId = document.candidatId,
      nom = document.nom,
      prenom = document.prenom,
      genre = document.genre,
      email = document.email,
      statutDemandeurEmploi = document.statutDemandeurEmploi,
      rechercheMetiersEvalues = document.rechercheMetiersEvalues,
      metiersEvalues = document.metiersEvalues.map(referentielMetier.metierParCode),
      rechercheAutresMetiers = document.rechercheAutresMetiers,
      metiersRecherches = document.metiersRecherches.flatMap(rechercheCandidatService.metierProposeParCode),
      contacteParAgenceInterim = document.contacteParAgenceInterim,
      contacteParOrganismeFormation = document.contacteParOrganismeFormation,
      commune = document.commune,
      codePostal = document.codePostal,
      rayonRecherche = document.rayonRecherche,
      numeroTelephone = document.numeroTelephone,
      dateInscription = document.dateInscription,
      dateDerniereConnexion = document.dateDerniereConnexion
    )
}
