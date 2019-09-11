package fr.poleemploi.perspectives.authentification.infra.autologin

import java.time.Clock

import fr.poleemploi.perspectives.authentification.infra.autologin.AutologinService.ALGORITHM
import fr.poleemploi.perspectives.candidat.CandidatId
import fr.poleemploi.perspectives.commun.domain.{Nom, Prenom}
import fr.poleemploi.perspectives.recruteur.RecruteurId
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}
import pdi.jwt.JwtJson
import play.api.libs.json.Json

import scala.util.Failure

class AutologinServiceSpec extends WordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var autologinService: AutologinService = _
  var autologinConfig: AutologinConfig = _

  private val issuer = "perspectives"
  private val secretKey = "my-secret-key"

  implicit val clock: Clock = Clock.systemUTC

  before {
    autologinConfig = mock[AutologinConfig]
    when(autologinConfig.issuer) thenReturn issuer
    when(autologinConfig.secretKey) thenReturn secretKey
    when(autologinConfig.expirationInSeconds) thenReturn 30
    autologinService = new AutologinService(autologinConfig)
  }

  "genererTokenCandidat" should {
    "generer un token JWT valide pour le candidat" in {
      // Given
      val prenom = "Marcel"
      val nom = "Patulacci"
      val identifiant = "123456"
      val jwtToken = autologinService.genererTokenCandidat(CandidatId(identifiant), Nom(nom), Prenom(prenom)).value

      // When
      val jwtClaim = JwtJson.decode(token = jwtToken, key = secretKey, algorithms = Seq(ALGORITHM))

      // Then
      jwtClaim.isSuccess mustBe true
      jwtClaim.get.subject.getOrElse("") mustBe identifiant
      jwtClaim.get.isValid mustBe true

      val autologinToken = Json.parse(jwtClaim.get.content).as[AutologinToken]
      autologinToken.identifiant mustBe identifiant
      autologinToken.nom.value mustBe nom
      autologinToken.prenom.value mustBe prenom
      autologinToken.typeUtilisateur mustBe TypeUtilisateur.CANDIDAT
    }
  }

  "genererTokenRecruteur" should {
    "generer un token JWT valide pour le recruteur" in {
      // Given
      val prenom = "Marcel"
      val nom = "Patulacci"
      val identifiant = "123456"
      val jwtToken = autologinService.genererTokenRecruteur(RecruteurId(identifiant), Nom(nom), Prenom(prenom)).value

      // When
      val jwtClaim = JwtJson.decode(token = jwtToken, key = secretKey, algorithms = Seq(ALGORITHM))

      // Then
      jwtClaim.isSuccess mustBe true
      jwtClaim.get.subject.getOrElse("") mustBe identifiant
      jwtClaim.get.isValid mustBe true

      val autologinToken = Json.parse(jwtClaim.get.content).as[AutologinToken]
      autologinToken.identifiant mustBe identifiant
      autologinToken.nom.value mustBe nom
      autologinToken.prenom.value mustBe prenom
      autologinToken.typeUtilisateur mustBe TypeUtilisateur.RECRUTEUR
    }
  }

  "extractAutologinToken" should {
    "doit lever une erreur quand le token n'est pas un token JWT valide" in {
      // Given & When
      val autologinToken = autologinService.extractAutologinToken("caca")

      // Then
      autologinToken.isFailure mustBe true
      autologinToken.asInstanceOf[Failure[AutologinToken]].exception.getMessage mustBe "Expected token [caca] to be composed of 2 or 3 parts separated by dots."
    }
    "doit lever une erreur quand le token n'est pas signé par la meme clé secrète" in {
      // Given
      val prenom = "Marcel"
      val nom = "Patulacci"
      val identifiant = "123456"
      val jwtToken = autologinService.genererTokenCandidat(CandidatId(identifiant), Nom(nom), Prenom(prenom)).value
      when(autologinConfig.secretKey).thenReturn("another-secret-key")

      // When
      val autologinToken = autologinService.extractAutologinToken(jwtToken)

      // Then
      autologinToken.isFailure mustBe true
      autologinToken.asInstanceOf[Failure[AutologinToken]].exception.getMessage mustBe "Invalid signature for this token or wrong algorithm."
    }
    "doit lever une erreur quand le token est expiré" in {
      // Given
      val prenom = "Marcel"
      val nom = "Patulacci"
      val identifiant = "123456"
      when(autologinConfig.expirationInSeconds).thenReturn(0)
      val jwtToken = autologinService.genererTokenCandidat(CandidatId(identifiant), Nom(nom), Prenom(prenom)).value

      // When
      val autologinToken = autologinService.extractAutologinToken(jwtToken)

      // Then
      autologinToken.isFailure mustBe true
      autologinToken.asInstanceOf[Failure[AutologinToken]].exception.getMessage must startWith("The token is expired")
    }
    "doit renvoyer le token d'autologin quand le token JWT est valide" in {
      // Given
      val prenom = "Marcel"
      val nom = "Patulacci"
      val identifiant = "123456"
      val jwtToken = autologinService.genererTokenCandidat(CandidatId(identifiant), Nom(nom), Prenom(prenom)).value

      // When
      val autologinToken = autologinService.extractAutologinToken(jwtToken)

      // Then
      autologinToken.isSuccess mustBe true
      autologinToken.get.identifiant mustBe identifiant
      autologinToken.get.prenom.value mustBe prenom
      autologinToken.get.nom.value mustBe nom
      autologinToken.get.typeUtilisateur mustBe TypeUtilisateur.CANDIDAT
    }
  }
}
