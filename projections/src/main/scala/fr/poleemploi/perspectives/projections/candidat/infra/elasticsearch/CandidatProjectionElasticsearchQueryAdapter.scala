package fr.poleemploi.perspectives.projections.candidat.infra.elasticsearch

import java.time.format.DateTimeFormatter

import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSource
import fr.poleemploi.perspectives.candidat._
import fr.poleemploi.perspectives.commun.infra.elasticsearch.EsConfig
import fr.poleemploi.perspectives.commun.infra.play.json.JsonFormats._
import fr.poleemploi.perspectives.commun.infra.ws.WSAdapter
import fr.poleemploi.perspectives.projections.candidat._
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// FIXME : perf referentiel metier
// FIXME : requete en passant des sous domaines + labels domaine ou sous secteur
class CandidatProjectionElasticsearchQueryAdapter(wsClient: WSClient,
                                                  esConfig: EsConfig,
                                                  mapping: CandidatProjectionElasticsearchQueryMapping) extends CandidatProjectionQuery with WSAdapter {

  import CandidatProjectionElasticsearchMapping._

  val baseUrl = s"${esConfig.host}:${esConfig.port}"

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  implicit val client: RestClient = RestClient.builder(buildHost).build()

  override def saisieCriteresRecherche(query: CandidatSaisieCriteresRechercheQuery): Future[CandidatSaisieCriteresRechercheQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .get()
      .flatMap(filtreStatutReponse(_))
      .flatMap(r => mapping.buildCandidatSaisieCriteresRechercheQueryResult((r.json \ "_source").as[CandidatSaisieCriteresRechercheDocument]))

  override def saisieDisponibilites(query: CandidatSaisieDisponibilitesQuery): Future[CandidatSaisieDisponibilitesQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/$docType/${query.candidatId.value}")
      .get()
      .flatMap(filtreStatutReponse(_))
      .map(r => mapping.buildCandidatSaisieDisponibilitesQueryResult((r.json \ "_source").as[CandidatSaisieDisponibilitesDocument]))

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

  override def listerPourBatchDisponibilites(query: CandidatsPourBatchDisponibilitesQuery): Future[CandidatsPourBatchDisponibilitesQueryResult] =
    if (query.candidatIds.isEmpty) Future(CandidatsPourBatchDisponibilitesQueryResult(Nil))
    else wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .withQueryStringParameters(
        ("_source", s"$candidat_id,$email")
      )
      .post(mapping.buildCandidatPourBatchDisponibilitesQuery(query))
      .map { response =>
        val candidats = (response.json \\ "_source").map(_.as[CandidatPourConseillerBatchDisponibilitesDocument]).toList

        CandidatsPourBatchDisponibilitesQueryResult(
          candidats = candidats.map(c => CandidatPourBatchDisponibilitesDto(
            candidatId = c.candidatId,
            email = c.email
          ))
        )
      }

  def secteursActivitesAvecCandidats(query: SecteursActivitesAvecCandidatsQuery): Future[SecteursActivitesAvecCandidatsQueryResult] =
    wsClient
      .url(s"$baseUrl/$indexName/_search")
      .withHttpHeaders(jsonContentType)
      .post(mapping.buildSecteursActivitesAvecCandidatQuery(query))
      .flatMap(r => mapping.buildSecteursActivitesAvecCandidatQueryResult(r.json))

  override def rechercherCandidats(query: RechercheCandidatsQuery): Future[RechercheCandidatQueryResult] =
    if (query.utiliserVersionDegradee) Future.successful(RechercheCandidatQueryResult.mock)
    else wsClient
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

  override def listerPourCsv(query: CandidatsPourCsvQuery.type): Future[CandidatsPourCsvQueryResult] =
    Future(CandidatsPourCsvQueryResult(
      ElasticsearchSource
        .create(
          indexName = indexName,
          typeName = docType,
          query = """{"match_all": {}}"""
        )
        .map { message =>
          val candidatPourStatistiquesDocument = Json.parse(message.source.toString()).as[CandidatPourStatistiquesDocument]
          mapping.buildCandidatPourStatistiquesDto(candidatPourStatistiquesDocument)
        }
        .filter(_.isDefined)
        .map(_.get)
    ))

  private def buildHost: HttpHost =
    new HttpHost(
      esConfig.host.replace("http://", "").replace("https://", ""),
      esConfig.port
    )
}
