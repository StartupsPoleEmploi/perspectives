package fr.poleemploi.perspectives.emailing.infra.mailjet

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import fr.poleemploi.perspectives.candidat.mrs.infra.peconnect.ImportMRSDHAEPEConnectAdapter
import fr.poleemploi.perspectives.commun.domain.Email
import fr.poleemploi.perspectives.emailing.domain.{MRSDHAEValideeProspectCandidat, MRSValideeProspectCandidat}
import fr.poleemploi.perspectives.emailing.infra.csv.ImportMRSValideeProspectCandidatCSVAdapter
import fr.poleemploi.perspectives.emailing.infra.sql.MailjetSqlAdapter
import fr.poleemploi.perspectives.emailing.infra.ws.MailjetWSAdapter
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers, Succeeded}

import scala.concurrent.Future

class MailjetImportProspectServiceSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)

  var importMRSValideeProspectCandidatCSVAdapter: ImportMRSValideeProspectCandidatCSVAdapter = _
  var importMRSDHAEPEConnectAdapter: ImportMRSDHAEPEConnectAdapter = _
  var mailjetWSAdapter: MailjetWSAdapter = _
  var mailjetSQLAdapter: MailjetSqlAdapter = _

  var mailjetImportProspectService: MailjetImportProspectService = _

  before {
    importMRSValideeProspectCandidatCSVAdapter = mock[ImportMRSValideeProspectCandidatCSVAdapter]
    importMRSDHAEPEConnectAdapter = mock[ImportMRSDHAEPEConnectAdapter]
    mailjetWSAdapter = mock[MailjetWSAdapter]
    mailjetSQLAdapter = mock[MailjetSqlAdapter]

    mailjetImportProspectService = new MailjetImportProspectService(
      actorSystem = actorSystem,
      importMRSValideeProspectCandidatCSVAdapter = importMRSValideeProspectCandidatCSVAdapter,
      importMRSDHAEPEConnectAdapter = importMRSDHAEPEConnectAdapter,
      mailjetWSAdapter = mailjetWSAdapter,
      mailjetSQLAdapter = mailjetSQLAdapter
    )

    when(mailjetWSAdapter.importerProspectsCandidats(ArgumentMatchers.any[Stream[MRSValideeProspectCandidat]]())) thenReturn Future.successful(())
  }

  "importerProspectsCandidat" should {
    "ne pas importer les prospects dont l'email représente un candidat déjà inscrit" in {
      // Given
      val prospectsInscrits = Stream.tabulate(10)(mockMRSProspectInscrit)
      val prospectsNonInscrits = Stream.tabulate(10)(mockMRSProspectNonInscrit)
      val prospectsDHAEInscrits = Stream.tabulate(10)(mockMRSDHAEProspectInscrit)
      val prospectsDHAENonInscrits = Stream.tabulate(10)(mockMRSDHAEProspectNonInscrit)
      when(importMRSValideeProspectCandidatCSVAdapter.importerProspectsCandidats) thenReturn Future.successful(prospectsInscrits ++ prospectsNonInscrits)
      when(importMRSDHAEPEConnectAdapter.importerProspectsCandidats) thenReturn Future.successful(prospectsDHAEInscrits ++ prospectsDHAENonInscrits)

      val candidatsInscrits: Source[CandidatMailjet, NotUsed] = Source(Stream.tabulate(50)(mockCandidatMailjet))
      when(mailjetSQLAdapter.streamCandidats) thenReturn candidatsInscrits

      // When
      val result = mailjetImportProspectService.importerProspectsCandidats

      // Then
      result.map(r => {
        val emailsProspectsCandidats = r.map(_.email)
        emailsProspectsCandidats.forall(m => prospectsNonInscrits.map(_.email).contains(m)) mustBe true
        emailsProspectsCandidats.forall(m => prospectsDHAENonInscrits.map(_.email).contains(m)) mustBe true
        emailsProspectsCandidats.forall(m => !prospectsInscrits.map(_.email).contains(m)) mustBe true
        emailsProspectsCandidats.forall(m => !prospectsDHAEInscrits.map(_.email).contains(m)) mustBe true
        Succeeded
      })
    }
  }

  private def mockMRSProspectInscrit(n: Int): MRSValideeProspectCandidat = {
    val mrs = mock[MRSValideeProspectCandidat]
    when(mrs.email) thenReturn Email(s"$n-inscrit@domain.com")
    mrs
  }

  private def mockMRSProspectNonInscrit(n: Int): MRSValideeProspectCandidat = {
    val mrs = mock[MRSValideeProspectCandidat]
    when(mrs.email) thenReturn Email(s"$n-non-inscrit@domain.com")
    mrs
  }

  private def mockMRSDHAEProspectInscrit(n: Int): MRSDHAEValideeProspectCandidat = {
    val mrs = mock[MRSDHAEValideeProspectCandidat]
    when(mrs.email) thenReturn Email(s"$n-inscrit@domain.com")
    mrs
  }

  private def mockMRSDHAEProspectNonInscrit(n: Int): MRSDHAEValideeProspectCandidat = {
    val mrs = mock[MRSDHAEValideeProspectCandidat]
    when(mrs.email) thenReturn Email(s"$n-non-inscrit@domain.com")
    mrs
  }

  private def mockCandidatMailjet(n: Int): CandidatMailjet = {
    val candidat = mock[CandidatMailjet]
    when(candidat.email) thenReturn Email(s"$n-inscrit@domain.com")
    candidat
  }
}
