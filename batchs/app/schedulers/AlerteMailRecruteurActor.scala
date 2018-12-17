package schedulers

import java.net.URLEncoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import akka.NotUsed
import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.recruteur.alerte._
import fr.poleemploi.perspectives.recruteur.alerte.domain.FrequenceAlerte

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AlerteMailRecruteurActor {

  final val name = "AlerteMailRecruteurActor"

  case object EnvoyerAlertesQuotidiennes

  case object EnvoyerAlertesHebdomadaires

  def props(emailingService: EmailingService,
            webappURL: String,
            alerteRecruteurProjection: AlerteRecruteurProjection,
            candidatProjection: CandidatProjection): Props =
    Props(new AlerteMailRecruteurActor(
      emailingService = emailingService,
      webappURL = webappURL,
      alerteRecruteurProjection = alerteRecruteurProjection,
      candidatProjection = candidatProjection
    ))
}

class AlerteMailRecruteurActor(emailingService: EmailingService,
                               webappURL: String,
                               alerteRecruteurProjection: AlerteRecruteurProjection,
                               candidatProjection: CandidatProjection) extends Actor with ActorLogging {

  import AlerteMailRecruteurActor._

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("eeee d MMMM yyyy", Locale.FRANCE)

  override def receive: Receive = {
    case EnvoyerAlertesQuotidiennes =>
      envoyerAlertes(
        alertes = alerteRecruteurProjection.alertesQuotidiennes,
        apresDateInscription = ZonedDateTime.now().minusDays(1L).withHour(0).withMinute(0).withSecond(0)
      )
    case EnvoyerAlertesHebdomadaires =>
      envoyerAlertes(
        alertes = alerteRecruteurProjection.alertesHebdomaraires,
        apresDateInscription = ZonedDateTime.now().minusDays(7L).withHour(0).withMinute(0).withSecond(0)
      )
    case Failure(t) =>
      log.error(t, "Erreur lors de l'envoi des alertes mails aux recruteurs")
  }

  private def envoyerAlertes(alertes: Source[AlerteRecruteurDto, NotUsed],
                             apresDateInscription: ZonedDateTime): Future[Unit] =
    alertes.runForeach(alerteRecruteurDto => {
      for {
        rechercheCandidatQueryResult <- candidatProjection.rechercherCandidats(RechercherCandidatsQuery(
          typeRecruteur = alerteRecruteurDto.typeRecruteur,
          codeROME = alerteRecruteurDto.metier.map(_.codeROME),
          codeSecteurActivite = alerteRecruteurDto.secteurActivite.map(_.code),
          coordonnees = alerteRecruteurDto.localisation.map(l => l.coordonnees),
          nbPagesACharger = 1,
          page = Some(KeysetRechercherCandidats(
            dateInscription = apresDateInscription.toEpochSecond,
            score = None,
            candidatId = None
          ))
        ))
        _ <-
          if (rechercheCandidatQueryResult.nbCandidatsTotal > 0)
            emailingService.envoyerAlerteMailRecruteur(buildAlerteMailRecruteur(
              alerteRecruteurDto = alerteRecruteurDto,
              rechercheCandidatQueryResult = rechercheCandidatQueryResult,
              apresDateInscription = apresDateInscription
            ))
          else
            Future.successful(())
      } yield ()
    }).map(_ => ()) pipeTo self

  private def buildAlerteMailRecruteur(alerteRecruteurDto: AlerteRecruteurDto,
                                       rechercheCandidatQueryResult: RechercheCandidatQueryResult,
                                       apresDateInscription: ZonedDateTime): AlerteMailRecruteur = {
    def nbCandidats: String = rechercheCandidatQueryResult.nbCandidatsTotal match {
      case x if x == 1 => s"1 nouveau candidat"
      case x if x > 1 => s"$x nouveaux candidats"
      case _ => ""
    }

    def nbCandidatsInscrits: String = rechercheCandidatQueryResult.nbCandidatsTotal match {
      case x if x == 1 => s"1 nouveau candidat s'est inscrit"
      case x if x > 1 => s"$x nouveaux candidats se sont inscrits"
      case _ => ""
    }

    def dateRechercheCandidat: String = alerteRecruteurDto.frequence match {
      case FrequenceAlerte.HEBDOMADAIRE => s"depuis le ${dateTimeFormatter.format(apresDateInscription)}"
      case _ => ""
    }

    def localisation: String = alerteRecruteurDto.localisation.map(l => s"à ${l.label}").getOrElse("")

    def metier: String = alerteRecruteurDto.metier.map(m => s"sur le métier ${m.label}").getOrElse("")

    def secteur: String = alerteRecruteurDto.secteurActivite.map(s => s"dans le secteur ${s.label}").getOrElse("")

    val textes = alerteRecruteurDto.metier.map(_ =>
      (
        s"$nbCandidats $metier $localisation",
        s"$nbCandidatsInscrits $metier $localisation $dateRechercheCandidat"
      )
    ).orElse(alerteRecruteurDto.secteurActivite.map(_ =>
      (
        s"$nbCandidats $secteur $localisation",
        s"$nbCandidatsInscrits $secteur $localisation $dateRechercheCandidat"
      )
    )).orElse(alerteRecruteurDto.localisation.map(_ =>
      (
        s"$nbCandidats $localisation",
        s"$nbCandidatsInscrits $localisation $dateRechercheCandidat"
      )
    )).getOrElse(throw new IllegalArgumentException("Type d'alerte non géré"))

    val lienConnexion = List(
      alerteRecruteurDto.metier.map(m => s"metier=${m.codeROME.value}"),
      alerteRecruteurDto.secteurActivite.map(s => s"secteurActivite=${s.code.value}"),
      alerteRecruteurDto.localisation.map(l => s"localisation=${URLEncoder.encode(l.label, "UTF-8")}&latitude=${l.coordonnees.latitude}&longitude=${l.coordonnees.longitude}")
    ).flatten.foldLeft(s"$webappURL/recruteur/recherche")((url, param) => s"$url&$param")
        .replaceFirst("&", "?")

    AlerteMailRecruteur(
      email = alerteRecruteurDto.email,
      sujet = textes._1,
      recapitulatifInscriptions = textes._2,
      lienConnexion = lienConnexion
    )
  }
}
