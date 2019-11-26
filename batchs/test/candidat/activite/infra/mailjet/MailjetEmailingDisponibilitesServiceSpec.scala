package candidat.activite.infra.mailjet

import java.time.LocalDate

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import com.typesafe.config.ConfigFactory
import fr.poleemploi.perspectives.authentification.infra.autologin.{AutologinService, JwtToken}
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.candidat.activite.domain.EmailingDisponibiliteCandidatAvecEmail
import fr.poleemploi.perspectives.candidat.activite.infra.DisponibiliteCandidat
import fr.poleemploi.perspectives.candidat.activite.infra.csv.{ActiviteCandidatCsv, ImportActiviteCandidatCsvAdapter}
import fr.poleemploi.perspectives.candidat.activite.infra.sql.DisponibiliteCandidatSqlAdapter
import fr.poleemploi.perspectives.commun.domain.{Email, Nom, Prenom}
import fr.poleemploi.perspectives.commun.infra.peconnect.sql.PEConnectSqlAdapter
import fr.poleemploi.perspectives.commun.infra.peconnect.{CandidatPEConnect, PEConnectId}
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import fr.poleemploi.perspectives.projections.candidat.{CandidatPourBatchDisponibilitesDto, CandidatQueryHandler, CandidatsPourBatchDisponibilitesQuery, CandidatsPourBatchDisponibilitesQueryResult}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

import scala.concurrent.Future

class MailjetEmailingDisponibilitesServiceSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName, ConfigFactory.empty())
  val baseUrl = "https://perspectives.pole-emploi.fr"

  var importActiviteCandidatCsvAdapter: ImportActiviteCandidatCsvAdapter = _
  var candidatQueryHandler: CandidatQueryHandler = _
  var mailjetWSAdapter: MailjetWSAdapter = _
  var peConnectSqlAdapter: PEConnectSqlAdapter = _
  var disponibiliteCandidatSqlAdapter: DisponibiliteCandidatSqlAdapter = _
  var autologinService: AutologinService = _

  var mailjetEmailingDisponibilitesService: MailjetEmailingDisponibilitesService = _

  val activiteCandidatCsvMock: ActiviteCandidatCsv = mockActiviteCandidatCsv(77)
  val candidatPEConnectMock: CandidatPEConnect = mockCandidatPEConnect
  val disponibiliteCandidatMock: DisponibiliteCandidat = mockDisponibiliteCandidat(LocalDate.now.minusMonths(1))
  val candidatPourBatchDisponibilitesDtoMock: CandidatPourBatchDisponibilitesDto = mockCandidatPourBatchDisponibilitesDto

  before {
    importActiviteCandidatCsvAdapter = mock[ImportActiviteCandidatCsvAdapter]
    candidatQueryHandler = mock[CandidatQueryHandler]
    mailjetWSAdapter = mock[MailjetWSAdapter]
    peConnectSqlAdapter = mock[PEConnectSqlAdapter]
    disponibiliteCandidatSqlAdapter = mock[DisponibiliteCandidatSqlAdapter]
    autologinService = mock[AutologinService]

    mailjetEmailingDisponibilitesService = new MailjetEmailingDisponibilitesService(
      actorSystem = actorSystem,
      baseUrl = baseUrl,
      importFileAdapter = importActiviteCandidatCsvAdapter,
      peConnectSqlAdapter = peConnectSqlAdapter,
      disponibiliteCandidatSqlAdapter = disponibiliteCandidatSqlAdapter,
      mailjetWSAdapter = mailjetWSAdapter,
      autologinService = autologinService,
      candidatQueryHandler = candidatQueryHandler
    )

    val candidatsCsvInscrits = Stream(activiteCandidatCsvMock)
    when(importActiviteCandidatCsvAdapter.importerActivitesCandidats) thenReturn Future.successful(candidatsCsvInscrits)

    val candidatsInscrits: Source[CandidatPEConnect, NotUsed] = Source(Stream(candidatPEConnectMock))
    when(peConnectSqlAdapter.streamCandidats) thenReturn candidatsInscrits

    val disponibilitesCandidats: Source[DisponibiliteCandidat, NotUsed] = Source(Stream(disponibiliteCandidatMock))
    when(disponibiliteCandidatSqlAdapter.streamDisponibilites) thenReturn disponibilitesCandidats

    when(candidatQueryHandler.handle(any[CandidatsPourBatchDisponibilitesQuery])) thenReturn Future.successful(CandidatsPourBatchDisponibilitesQueryResult(List(candidatPourBatchDisponibilitesDtoMock)))

    when(mailjetWSAdapter.envoyerDisponibilitesCandidat(any[String], any[Stream[EmailingDisponibiliteCandidatAvecEmail]]())) thenReturn Future.successful(())
    when(disponibiliteCandidatSqlAdapter.ajouter(any[Stream[CandidatId]])) thenReturn Future.successful(())
    when(autologinService.genererTokenCandidat(any[CandidatId], any[Nom], any[Prenom], any[Email])) thenReturn JwtToken("token")
  }

  "envoyerEmailsDisponibilites" should {
    "envoyer un mail aux candidats qui ont travaille plus de 60 heures, qui sont disponibles dans Perspectives et a qui on n'a pas deja envoye un mail ce mois" in {
      // Given & When
      val result = mailjetEmailingDisponibilitesService.envoyerEmailsDisponibilites

      // Then
      result.map(r => {
        r.size mustBe 1
        r.head.candidatId.value mustBe "999999"
        r.head.email.value mustBe "email@candidat.fr"
        r.head.nom.value mustBe "Patulacci"
        r.head.prenom.value mustBe "Marcel"
        r.head.autologinToken.value mustBe "token"
        Succeeded
      })
    }
    "ne pas envoyer de mail aux candidats qui ont travaille moins de 60 heures" in {
      // Given
      when(activiteCandidatCsvMock.nbHeuresTravaillees) thenReturn 59

      // When
      val result = mailjetEmailingDisponibilitesService.envoyerEmailsDisponibilites

      // Then
      result.map(r => {
        r.isEmpty mustBe true
        Succeeded
      })
    }
    "ne pas envoyer de mail aux candidats qui ne sont pas inscrits sur Perspectives" in {
      // Given
      when(peConnectSqlAdapter.streamCandidats) thenReturn Source(Stream.empty)

      // When
      val result = mailjetEmailingDisponibilitesService.envoyerEmailsDisponibilites

      // Then
      result.map(r => {
        r.isEmpty mustBe true
        Succeeded
      })
    }
    "ne pas envoyer de mail aux candidats qui ne sont pas disponibles dans Perspectives" in {
      // Given
      when(candidatQueryHandler.handle(any[CandidatsPourBatchDisponibilitesQuery])) thenReturn Future.successful(CandidatsPourBatchDisponibilitesQueryResult(Nil))

      // When
      val result = mailjetEmailingDisponibilitesService.envoyerEmailsDisponibilites

      // Then
      result.map(r => {
        r.isEmpty mustBe true
        Succeeded
      })
    }
    "ne pas envoyer de mail aux candidats a qui on a deja envoye un mail ce mois" in {
      // Given
      when(disponibiliteCandidatMock.dateDernierEnvoiMail) thenReturn LocalDate.now

      // When
      val result = mailjetEmailingDisponibilitesService.envoyerEmailsDisponibilites

      // Then
      result.map(r => {
        r.isEmpty mustBe true
        Succeeded
      })
    }
  }

  private def mockActiviteCandidatCsv(nbHeuresTravaillees: Int) = {
    val activiteCandidatCsv = mock[ActiviteCandidatCsv]
    when(activiteCandidatCsv.peConnectId) thenReturn PEConnectId("123456")
    when(activiteCandidatCsv.nom) thenReturn Nom("Patulacci")
    when(activiteCandidatCsv.prenom) thenReturn Prenom("Marcel")
    when(activiteCandidatCsv.nbHeuresTravaillees) thenReturn nbHeuresTravaillees
    when(activiteCandidatCsv.dateActualisation) thenReturn LocalDate.now()
    activiteCandidatCsv
  }

  private def mockCandidatPEConnect = {
    val candidatPEConnect = mock[CandidatPEConnect]
    when(candidatPEConnect.peConnectId) thenReturn PEConnectId("123456")
    when(candidatPEConnect.candidatId) thenReturn CandidatId("999999")
    candidatPEConnect
  }

  private def mockCandidatPourBatchDisponibilitesDto = {
    val candidatPourBatchDisponibilitesDto = mock[CandidatPourBatchDisponibilitesDto]
    when(candidatPourBatchDisponibilitesDto.candidatId) thenReturn CandidatId("999999")
    when(candidatPourBatchDisponibilitesDto.email) thenReturn Email("email@candidat.fr")
    candidatPourBatchDisponibilitesDto
  }

  private def mockDisponibiliteCandidat(dateDernierEnvoi: LocalDate) = {
    val disponibiliteCandidat = mock[DisponibiliteCandidat]
    when(disponibiliteCandidat.candidatId) thenReturn CandidatId("999999")
    when(disponibiliteCandidat.dateDernierEnvoiMail) thenReturn dateDernierEnvoi
    disponibiliteCandidat
  }
}
