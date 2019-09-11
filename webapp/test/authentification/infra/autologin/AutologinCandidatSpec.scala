package authentification.infra.autologin

import fr.poleemploi.perspectives.authentification.infra.autologin.{AutologinService, AutologinToken, TypeUtilisateur}
import fr.poleemploi.perspectives.candidat.{AutologgerCandidatCommand, CandidatCommandHandler}
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.projections.candidat.{CandidatQueryHandler, ExisteCandidatQuery, ExisteCandidatQueryResult}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AutologinCandidatSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var autologinCandidat: AutologinCandidat = _
  var _autologinService: AutologinService = _
  var _candidatQueryHandler: CandidatQueryHandler = _
  var _candidatCommandHandler: CandidatCommandHandler = _

  val autologinTokenCandidat = AutologinToken("123456", Nom("Patulacci"), Prenom("Marcel"), TypeUtilisateur.CANDIDAT)
  val tokenQueryParam = "xxxx"
  val request: Request[AnyContentAsEmpty.type] = FakeRequest("GET", s"/ma-super-url?param1=1&token=$tokenQueryParam&param2=2")

  before {
    _autologinService = mock[AutologinService]
    _candidatQueryHandler = mock[CandidatQueryHandler]
    _candidatCommandHandler = mock[CandidatCommandHandler]

    when(_autologinService.extractAutologinToken(anyString())).thenReturn(Success(autologinTokenCandidat))
    when(_candidatQueryHandler.handle(any[ExisteCandidatQuery])).thenReturn(Future.successful(ExisteCandidatQueryResult(true)))

    autologinCandidat = new AutologinCandidat {
      override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

      override def autologinService: AutologinService = _autologinService

      override def candidatQueryHandler: CandidatQueryHandler = _candidatQueryHandler

      override def candidatCommandHandler: CandidatCommandHandler = _candidatCommandHandler
    }
  }

  "extraireCandidatAutologge" should {
    "ne pas renvoyer de candidat autologge quand pas de token dans l'URL" in {
      // Given
      val request = FakeRequest("GET", "/ma-super-url?param1=1&param2=2")

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => candidatAutologge mustBe None)
    }
    "ne pas renvoyer de candidat autologge quand le token dans l'URL est invalide" in {
      // Given
      when(_autologinService.extractAutologinToken(anyString())).thenReturn(Failure(new IllegalArgumentException("token invalide")))

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => candidatAutologge mustBe None)
    }
    "ne pas renvoyer de candidat autologge quand le token dans l'URL concerne un recruteur" in {
      // Given
      val autologinTokenRecruteur = autologinTokenCandidat.copy(typeUtilisateur = TypeUtilisateur.RECRUTEUR)
      when(_autologinService.extractAutologinToken(anyString())).thenReturn(Success(autologinTokenRecruteur))

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => candidatAutologge mustBe None)
    }
    "ne pas renvoyer de candidat autologge quand la query ExisteCandidatQuery est en erreur" in {
      // Given
      when(_candidatQueryHandler.handle(any[ExisteCandidatQuery])).thenReturn(Future.failed(new IllegalArgumentException("erreur de la projection")))

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => candidatAutologge mustBe None)
    }
    "ne pas renvoyer de candidat autologge quand l'utilisateur associe au token n'existe pas" in {
      // Given
      when(_candidatQueryHandler.handle(any[ExisteCandidatQuery])).thenReturn(Future.successful(ExisteCandidatQueryResult(false)))

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => candidatAutologge mustBe None)
    }
    "rne pas renvoyer de candidat autologge quand la commande qui genere l evenement de connexion leve une erreur" in {
      // Given
      when(_candidatCommandHandler.handle(any[AutologgerCandidatCommand])).thenReturn(Future.failed(new IllegalArgumentException("erreur execution commande AutologgerCandidatCommand")))

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => candidatAutologge mustBe None)
    }
    "renvoyer le candidat autologge avec ses informations" in {
      // Given
      when(_candidatCommandHandler.handle(any[AutologgerCandidatCommand])).thenReturn(Future.successful((): Unit))

      // When
      val future = autologinCandidat.extraireCandidatAutologge(request)

      // Then
      future.map(candidatAutologge => {
        candidatAutologge.get.autologinToken.value mustBe tokenQueryParam
        candidatAutologge.get.candidatAuthentifie.candidatId.value mustBe autologinTokenCandidat.identifiant
        candidatAutologge.get.candidatAuthentifie.nom mustBe autologinTokenCandidat.nom
        candidatAutologge.get.candidatAuthentifie.prenom mustBe autologinTokenCandidat.prenom
      })
    }
  }
}
