package controllers.admin

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import authentification._
import conf.WebAppConfig
import fr.poleemploi.perspectives.projections.candidat.infra.csv.CandidatInscritCsvGenerator
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourCsvQuery}
import fr.poleemploi.perspectives.prospect.domain.ReferentielProspectCandidat
import fr.poleemploi.perspectives.prospect.infra.csv.ProspectCandidatCsvGenerator
import fr.poleemploi.perspectives.rome.domain.ReferentielRome
import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.http.HttpEntity
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class AdminController @Inject()(cc: ControllerComponents,
                                webAppConfig: WebAppConfig,
                                referentielRome: ReferentielRome,
                                prospectCandidatCsvGenerator: ProspectCandidatCsvGenerator,
                                candidatInscritCsvGenerator: CandidatInscritCsvGenerator,
                                referentielProspectCandidat: ReferentielProspectCandidat,
                                candidatQueryHandler: CandidatQueryHandler,
                                messagesAction: MessagesActionBuilder,
                                conseillerAdminAuthentifieAction: ConseillerAdminAuthentifieAction,
                                val actorSystem: ActorSystem)
                               (implicit exec: ExecutionContext) extends AbstractController(cc) with Logging {

  import AdminController._

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

  def rechargerAppellationsMetiers: Action[AnyContent] = Action.async { implicit request =>
    if (isAutorise(request))
      referentielRome.rechargerAppellations.map(_ => NoContent)
    else Future(Unauthorized)
  }

  def genererCsvProspectsCandidats(dateMax: Option[String]): Action[AnyContent] = Action { implicit request =>
    if (isAutorise(request)) {
      val dateMaxEvaluationMrs = dateMax.flatMap(d => Try(LocalDate.parse(d, dateFormatter)).toOption)
      Result(
        header = ResponseHeader(OK, Map(CONTENT_DISPOSITION -> s"attachment; filename=$PROSPECTS_CANDIDATS_CSV_FILENAME")),
        body = HttpEntity.Streamed(prospectCandidatCsvGenerator.generate(referentielProspectCandidat.streamProspectsCandidats(dateMaxEvaluationMrs)), None, None)
      )
    } else Unauthorized
  }

  def genererCsvCandidatsInscrits: Action[AnyContent] = Action.async { implicit request =>
    if (isAutorise(request))
      candidatQueryHandler.handle(CandidatsPourCsvQuery).map(result =>
        Result(
          header = ResponseHeader(OK, Map(CONTENT_DISPOSITION -> s"attachment; filename=$CANDIDATS_INSCRITS_CSV_FILENAME")),
          body = HttpEntity.Streamed(candidatInscritCsvGenerator.generate(result.source), None, None)
        )
      )
    else Future.successful(Unauthorized)
  }

  private def isAutorise(request: Request[AnyContent]): Boolean =
    request.getQueryString(API_KEY).contains(webAppConfig.adminApiKey)
}

object AdminController {
  private val API_KEY = "apiKey"

  private val PROSPECTS_CANDIDATS_CSV_FILENAME = "prospects_candidats.csv"

  private val CANDIDATS_INSCRITS_CSV_FILENAME = "candidats_inscrits.csv"
}
