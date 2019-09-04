package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.format.DateTimeFormatter

import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.projections.candidat._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME : perf referentiel metier
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
        criteres_recherche -> Json.obj(
          "metiers_valides" -> JsArray.empty,
          "metiers" -> JsArray.empty,
          "domaines_professionnels" -> JsArray.empty
        ),
        centres_interet -> JsArray.empty,
        langues -> JsArray.empty,
        permis -> JsArray.empty,
        savoir_etre -> JsArray.empty,
        savoir_faire -> JsArray.empty,
        formations -> JsArray.empty,
        experiences_professionnelles -> JsArray.empty
      )).map(_ => ())

  override def onCandidatConnecteEvent(event: CandidatConnecteEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      date_derniere_connexion -> dateTimeFormatter.format(event.date)
    ))

  override def onCandidatAutologgeEvent(event: CandidatAutologgeEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      date_derniere_connexion -> dateTimeFormatter.format(event.date)
    ))

  override def onProfilModifieEvent(event: ProfilCandidatModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      nom -> event.nom,
      prenom -> event.prenom,
      genre -> event.genre,
      email -> event.email
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
          "rayon" -> event.localisationRecherche.rayonRecherche.map(mapping.buildRayonRechercheDocument),
          "zone" -> mapping.buildZoneDocument(
            coordonnees = event.localisationRecherche.coordonnees,
            rayonRecherche = event.localisationRecherche.rayonRecherche
          ),
          "temps_travail" -> event.tempsTravailRecherche
        )
      ))

  override def onNumeroTelephoneModifieEvent(event: NumeroTelephoneModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      numero_telephone -> event.numeroTelephone
    ))

  override def onStatutDemandeurEmploiModifieEvent(event: StatutDemandeurEmploiModifieEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      statut_demandeur_emploi -> event.statutDemandeurEmploi
    ))

  override def onCentresInteretModifiesEvent(event: CentresInteretModifiesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      centres_interet -> event.centresInteret
    ))

  override def onLanguesModifieesEvent(event: LanguesModifieesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      langues -> event.langues
    ))

  override def onPermisModifiesEvent(event: PermisModifiesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      permis -> event.permis
    ))

  override def onSavoirEtreModifiesEvent(event: SavoirEtreModifiesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      savoir_etre -> event.savoirEtre
    ))

  override def onSavoirFaireModifiesEvent(event: SavoirFaireModifiesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      savoir_faire -> event.savoirFaire
    ))

  override def onFormationsModifieesEvent(event: FormationsModifieesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      formations -> event.formations.map(mapping.buildFormationDocument)
    ))

  override def onExperiencesProfessionnellesModifieesEvent(event: ExperiencesProfessionnellesModifieesEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      experiences_professionnelles -> event.experiencesProfessionnelles.map(mapping.buildExperienceProfessionnelleDocument)
    ))

  override def onCVAjouteEvent(event: CVAjouteEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      cv_id -> event.cvId,
      cv_type_media -> event.typeMedia
    ))

  override def onCVRemplaceEvent(event: CVRemplaceEvent): Future[Unit] =
    update(event.candidatId, Json.obj(
      cv_id -> event.cvId,
      cv_type_media -> event.typeMedia
    ))

  override def onMRSAjouteeEvent(event: MRSAjouteeEvent): Future[Unit] =
    for {
      metiersValides <- wsClient
        .url(s"$baseUrl/$indexName/$docType/${event.candidatId.value}")
        .withQueryStringParameters(refreshParam, ("_source", s"$metiers_valides"))
        .get()
        .flatMap(filtreStatutReponse(_))
        .map(r => (r.json \ "_source" \ s"$metiers_valides").as[Set[MetierValideDocument]])
      _ <- update(event.candidatId, Json.obj(
        metiers_valides -> (metiersValides + MetierValideDocument(
          metier = event.codeROME,
          habiletes = event.habiletes,
          departement = event.departement,
          isDHAE = event.isDHAE
        ))
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
      .flatMap(r => mapping.buildCandidatSaisieCriteresRechercheQueryResult((r.json \ "_source").as[CandidatSaisieCriteresRechercheDocument]))

  override def localisation(query: CandidatLocalisationQuery): Future[CandidatLocalisationQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$commune,$code_postal,$latitude,$longitude")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildCandidatLocalisationQueryResult((r.json \ "_source").as[CandidatLocalisationDocument]))

  override def metiersValides(query: CandidatMetiersValidesQuery): Future[CandidatMetiersValidesQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$metiers_valides")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .flatMap(r => mapping.buildMetiersValidesQueryResult((r.json \ "_source" \ s"$metiers_valides").as[Set[MetierValideDocument]]))

  override def rechercheOffre(query: CandidatPourRechercheOffreQuery): Future[CandidatPourRechercheOffreQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$candidat_id,$metiers_valides,$criteres_recherche,$cv_id")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .flatMap(r => mapping.buildCandidatPourRechercheOffreQueryResult((r.json \ "_source").as[CandidatPourRechercheOffreDocument]))

  override def candidatContactRecruteur(candidatId: CandidatId): Future[CandidatContactRecruteurQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${candidatId.value}")
      .withQueryStringParameters(
        ("_source", s"$contact_recruteur,$contact_formation")
      )
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildContactRecruteurQueryResult((r.json \ "_source").as[CandidatContactRecruteurDocument]))

  override def listerPourConseiller(query: CandidatsPourConseillerQuery): Future[CandidatsPourConseillerQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildCandidatPourConseillerQuery(query))
      .flatMap { response =>
        val nbCandidatsTotal = (response.json \ "hits" \ "total").as[Int]
        val candidats = (response.json \\ "_source").map(_.as[CandidatPourConseillerDocument]).toList

        mapping.buildCandidatPourConseillerDto(candidats).map(dtos =>
          CandidatsPourConseillerQueryResult(
            nbCandidatsTotal = nbCandidatsTotal,
            candidats = dtos,
            pageSuivante =
              if (candidats.size < query.nbCandidatsParPage)
                None
              else {
                val sort = (response.json \\ "sort").toList.last
                Some(KeysetCandidatsPourConseiller(
                  dateInscription = (sort \ 0).as[Long],
                  candidatId = (sort \ 1).as[CandidatId]
                ))
              }
          )
        )
      }

  override def existeCandidat(query: ExisteCandidatQuery): Future[ExisteCandidatQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(_ => ExisteCandidatQueryResult(true))
    .recover {
      case _ => ExisteCandidatQueryResult(false)
    }

  def secteursActivitesAvecCandidats(query: SecteursActivitesAvecCandidatsQuery): Future[SecteursActivitesAvecCandidatsQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildSecteursActivitesAvecCandidatQuery(query))
      .flatMap(r => mapping.buildSecteursActivitesAvecCandidatQueryResult(r.json))

  override def rechercherCandidats(query: RechercheCandidatsQuery): Future[RechercheCandidatQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildRechercheCandidatsQuery(query))
      .flatMap { response =>
        val json = response.json
        val hits = (json \ "hits" \ "hits").as[JsArray]
        val candidats = (hits \\ "_source").take(query.nbCandidatsParPage).map(_.as[CandidatPourRecruteurDocument])
        val nbCandidatsTotal = (json \ "hits" \ "total").as[Int]

        mapping.buildCandidatsRechercheDto(candidats).map(candidats =>
          RechercheCandidatQueryResult(
            candidats = candidats,
            nbCandidatsTotal = nbCandidatsTotal,
            pagesSuivantes =
              if (nbCandidatsTotal <= query.nbCandidatsParPage)
                Nil
              else
                (hits \\ "sort").zipWithIndex
                  .filter(v => (v._2 + 1) % query.nbCandidatsParPage == 0)
                  .map(v =>
                    if (query.codeSecteurActivite.isDefined || query.codeROME.isDefined)
                      KeysetCandidatPourRecruteur(
                        score = Some((v._1 \ 0).as[Int]),
                        dateInscription = (v._1 \ 1).as[Long],
                        candidatId = (v._1 \ 2).as[CandidatId]
                      )
                    else
                      KeysetCandidatPourRecruteur(
                        score = None,
                        dateInscription = (v._1 \ 0).as[Long],
                        candidatId = (v._1 \ 1).as[CandidatId]
                      )
                  ).toList
          )
        )
      }

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
