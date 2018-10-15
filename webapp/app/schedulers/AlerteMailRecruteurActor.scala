package schedulers

import java.time.ZonedDateTime

import akka.NotUsed
import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.emailing.domain._
import fr.poleemploi.perspectives.projections.candidat._
import fr.poleemploi.perspectives.projections.recruteur.alerte._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AlerteMailRecruteurActor {

  final val name = "AlerteMailRecruteurActor"

  case object EnvoyerAlertesQuotidiennes

  case object EnvoyerAlertesHebdomadaires

  def props(emailingService: EmailingService,
            baseURL: String,
            alerteRecruteurProjection: AlerteRecruteurProjection,
            candidatQueryHandler: CandidatQueryHandler): Props =
    Props(new AlerteMailRecruteurActor(
      emailingService = emailingService,
      baseURL = baseURL,
      alerteRecruteurProjection = alerteRecruteurProjection,
      candidatQueryHandler = candidatQueryHandler
    ))
}

class AlerteMailRecruteurActor(emailingService: EmailingService,
                               baseURL: String,
                               alerteRecruteurProjection: AlerteRecruteurProjection,
                               candidatQueryHandler: CandidatQueryHandler) extends Actor with ActorLogging {

  import AlerteMailRecruteurActor._

  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {
    case EnvoyerAlertesQuotidiennes =>
      envoyerAlertes(
        alertes = alerteRecruteurProjection.alertesQuotidiennes,
        apresDateInscription = ZonedDateTime.now().minusDays(1L)
      )
    case EnvoyerAlertesHebdomadaires =>
      envoyerAlertes(
        alertes = alerteRecruteurProjection.alertesHebdomaraires,
        apresDateInscription = ZonedDateTime.now().minusDays(7L)
      )
    case Failure(t) =>
      log.error(t, "Erreur lors de l'envoi des alertes mails aux recruteurs")
  }

  private def envoyerAlertes(alertes: Source[AlerteRecruteurDto, NotUsed],
                             apresDateInscription: ZonedDateTime): Future[Unit] =
    alertes.runForeach(alerteRecruteurDto => {
      val rechercherCandidatsQuery = buildRechercherCandidatsQuery(alerteRecruteurDto, apresDateInscription)
      for {
        resultatRechercheCandidat <- candidatQueryHandler.rechercherCandidats(rechercherCandidatsQuery)
        _ <- emailingService.envoyerAlerteMailRecruteur(buildAlerteMailRecruteur(
          alerteRecruteurDto = alerteRecruteurDto,
          resultatRechercheCandidat = resultatRechercheCandidat,
          apresDateInscription = apresDateInscription
        ))
      } yield ()
    }).map(_ => ()) pipeTo self

  private def buildRechercherCandidatsQuery(alerteRecruteurDto: AlerteRecruteurDto,
                                            apresDateInscription: ZonedDateTime): RechercherCandidatsQuery =
    alerteRecruteurDto match {
      case a: AlerteRecruteurMetierDto =>
        RechercherCandidatsParMetierQuery(
          typeRecruteur = a.typeRecruteur,
          codeROME = a.metier.codeROME,
          codeDepartement = a.departement.map(_.code),
          apresDateInscription = Some(apresDateInscription)
        )
      case a: AlerteRecruteurSecteurDto =>
        RechercherCandidatsParSecteurQuery(
          typeRecruteur = a.typeRecruteur,
          codeSecteurActivite = a.secteurActivite.code,
          codeDepartement = a.departement.map(_.code),
          apresDateInscription = Some(apresDateInscription)
        )
      case a: AlerteRecruteurDepartementDto =>
        RechercherCandidatsParDepartementQuery(
          typeRecruteur = a.typeRecruteur,
          codeDepartement = a.departement.code,
          apresDateInscription = Some(apresDateInscription)
        )
    }

  private def buildAlerteMailRecruteur(alerteRecruteurDto: AlerteRecruteurDto,
                                       resultatRechercheCandidat: ResultatRechercheCandidat,
                                       apresDateInscription: ZonedDateTime): AlerteMailRecruteur =
    alerteRecruteurDto match {
      case a: AlerteRecruteurSecteurDto =>
        AlerteMailRecruteurSecteur(
          prenom = a.prenom,
          email = a.email,
          frequence = a.frequence,
          nbCandidats = resultatRechercheCandidat.nbCandidats,
          apresDateInscription = apresDateInscription,
          departement = a.departement,
          secteurActivite = a.secteurActivite,
          lienConnexion = s"$baseURL/recruteur/recherche?secteurActivite=${a.secteurActivite.code.value}${a.departement.map(c => s"&departement=${c.code.value}").getOrElse("")}"
        )
      case a: AlerteRecruteurMetierDto =>
        AlerteMailRecruteurMetier(
          prenom = a.prenom,
          email = a.email,
          frequence = a.frequence,
          nbCandidats = resultatRechercheCandidat.nbCandidats,
          apresDateInscription = apresDateInscription,
          departement = a.departement,
          metier = a.metier,
          lienConnexion = s"$baseURL/recruteur/recherche?metier=${a.metier.codeROME.value}${a.departement.map(c => s"&departement=${c.code.value}").getOrElse("")}"
        )
      case a: AlerteRecruteurDepartementDto =>
        AlerteMailRecruteurDepartement(
          prenom = a.prenom,
          email = a.email,
          frequence = a.frequence,
          nbCandidats = resultatRechercheCandidat.nbCandidats,
          apresDateInscription = apresDateInscription,
          departement = a.departement,
          lienConnexion = s"$baseURL/recruteur/recherche?departement=${a.departement.code.value}"
        )
    }
}
