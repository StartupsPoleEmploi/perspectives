package candidat.activite.infra.mailjet

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import candidat.activite.domain.EmailingDisponibilitesService
import candidat.activite.infra.mailjet.MailjetEmailingDisponibilitesService.NB_HEURES_MIN
import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinService
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail
import fr.poleemploi.perspectives.candidat.activite.infra.csv.{ActiviteCandidatCsv, ImportActiviteCandidatCsvAdapter}
import fr.poleemploi.perspectives.candidat.activite.infra.sql.DisponibiliteCandidatSqlAdapter
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectId}
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, CandidatsPourBatchDisponibilitesQuery, CandidatsPourBatchDisponibilitesQueryResult}
import play.api.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MailjetEmailingDisponibilitesService(actorSystem: ActorSystem,
                                           baseUrl: String,
                                           importFileAdapter: ImportActiviteCandidatCsvAdapter,
                                           peConnectSqlAdapter: PEConnectSqlAdapter,
                                           disponibiliteCandidatSqlAdapter: DisponibiliteCandidatSqlAdapter,
                                           candidatQueryHandler: CandidatQueryHandler,
                                           autologinService: AutologinService,
                                           mailjetWSAdapter: MailjetWSAdapter) extends EmailingDisponibilitesService with Logging {

  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  override def envoyerEmailsDisponibilites: Future[Stream[EmailingDisponibiliteCandidatAvecEmail]] =
    for {
      activitesCandidats <- importFileAdapter.importerActivitesCandidats
        .map(
          _.filter(_.nbHeuresTravaillees >= NB_HEURES_MIN)
            .toList
            .groupBy(_.peConnectId)
        )
      now = LocalDate.now()
      _ = logger.debug(s"Nombre de candidats ayant travaillé plus de $NB_HEURES_MIN heures dans le mois : ${activitesCandidats.keySet.size}")
      emailingDisponibilitesCandidats <- peConnectSqlAdapter
        .streamCandidats
        .map(candidat => buildEmailingDisponibilitesCandidats(candidat, activitesCandidats))
        .runWith(Sink.collection)
        .map(_.flatten)
      candidatIdsAvecDispoDejaEnvoyeeCeMois <- disponibiliteCandidatSqlAdapter
        .streamDisponibilites
        .filter(d => isMoisCourant(now, d.dateDernierEnvoiMail))
        .map(_.candidatId)
        .runWith(Sink.collection)
      candidatsSansDispoEnvoyeeCeMois = emailingDisponibilitesCandidats
        .filterNot(can => candidatIdsAvecDispoDejaEnvoyeeCeMois.contains(can.candidatId))
      candidatsPourBatchDisponibilitesQueryResult <- candidatQueryHandler.handle(CandidatsPourBatchDisponibilitesQuery(candidatsSansDispoEnvoyeeCeMois.map(_.candidatId)))
      candidatsAvecEmail = buildCandidatsAvecEmail(candidatsPourBatchDisponibilitesQueryResult, candidatsSansDispoEnvoyeeCeMois)
      _ = logger.debug(s"Nombre de candidats disponibles et dont le mail de dispo n'a pas été envoyé ce mois : ${candidatsAvecEmail.size}")
      _ <- mailjetWSAdapter.envoyerDisponibilitesCandidat(baseUrl, candidatsAvecEmail)
      _ <- disponibiliteCandidatSqlAdapter.ajouter(candidatsAvecEmail.map(_.candidatId))
    } yield candidatsAvecEmail.toStream

  private def buildCandidatsAvecEmail(queryResult: CandidatsPourBatchDisponibilitesQueryResult,
                                      candidats: Seq[EmailingDisponibiliteCandidat]): Seq[EmailingDisponibiliteCandidatAvecEmail] =
    candidats.flatMap(c =>
      queryResult.candidats.find(_.candidatId == c.candidatId).map(c2 => EmailingDisponibiliteCandidatAvecEmail(
        candidatId = c.candidatId,
        nom = c.nom,
        prenom = c.prenom,
        email = c2.email,
        autologinToken = autologinService.genererTokenCandidat(c.candidatId, c.nom, c.prenom, c2.email)
      ))
    )

  private def buildEmailingDisponibilitesCandidats(candidatPEConnect: CandidatPEConnect,
                                                   activiteCandidatParIdPEConnect: Map[PEConnectId, List[ActiviteCandidatCsv]]): List[EmailingDisponibiliteCandidat] =
    activiteCandidatParIdPEConnect.get(candidatPEConnect.peConnectId).map(_.map(activiteCandidat =>
      EmailingDisponibiliteCandidat(
        candidatId = candidatPEConnect.candidatId,
        nom = activiteCandidat.nom,
        prenom = activiteCandidat.prenom
      )
    )).getOrElse(Nil)

  private def isMoisCourant(now: LocalDate, localDate: LocalDate): Boolean =
    localDate.getYear == now.getYear && localDate.getMonthValue == now.getMonthValue
}

private case class EmailingDisponibiliteCandidat(candidatId: CandidatId,
                                                 nom: Nom,
                                                 prenom: Prenom)

object MailjetEmailingDisponibilitesService {
  private val NB_HEURES_MIN = 60
}
