package fr.poleemploi.perspectives.candidat.mrs.infra.csv

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.CodeROME
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class MRSValideesCSVAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val mrsValideesCSVAdapter: MRSValideesCSVAdapter = new MRSValideesCSVAdapter(actorSystem)

  "integrerMRSValidees" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
        """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas d'identité externe" in {
      // Given
      val source = Source.single(
        ByteString(
        """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
          |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";;"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un identifiant PEConnect invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";NULL;"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas la date de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le code ROME de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";;"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le statut de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;;"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "integrer la ligne lorsqu'elle contient toutes les informations" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;
            |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer l'identifiant externe de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.peConnectId mustBe PEConnectId("28d0b75a-b694-4de3-8849-18bfbfebd729")
      })
    }
    "integrer la date de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.dateEvaluation mustBe LocalDate.parse("2018-01-09"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente une mrs validée" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeROME mustBe CodeROME("H2909"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente l'entrée en formation du candidat (mrs validée puis entrée en formation)" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeROME mustBe CodeROME("H2909"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente l'embauche d'un candidat (mrs validée suivie d'une embauche)" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostallocalite;dc_codealeinscription;dc_adresseemail;dc_rome_id;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"12345678910";8031234567;"NOM";"PRENOM";"0701020304";2018-01-09 00:00:00.0;"VEM";"44200 NANTES";"44155";"PRENOM.NOM@gmail.com";"H2909";"28d0b75a-b694-4de3-8849-18bfbfebd729";"G1502";;;""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeROME mustBe CodeROME("H2909"))
    }
  }
}
