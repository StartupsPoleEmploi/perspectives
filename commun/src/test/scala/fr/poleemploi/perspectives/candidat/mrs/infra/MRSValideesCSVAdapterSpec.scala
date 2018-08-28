package fr.poleemploi.perspectives.candidat.mrs.infra

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.candidat.mrs.infra.csv.MRSValideesCSVAdapter
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
        """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2""".stripMargin)
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
        """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
          |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";2018-02-06 00:00:00.0;"";"J1305";"08";"N1101";"00"
          |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de date pour la mrs validée" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";;"28d0b75a-b694-4de3-8849-b618184bcf10";"J1305";"08";"N1101";"00"
            |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de code ROME de mrs validée" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";;"28d0b75a-b694-4de3-8849-b618184bcf10";"J1305";"08";"N1101";"00"
            |""".stripMargin)
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
          """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";2018-02-06 00:00:00.0;"28d0b75a-b694-4de3-8849-b618184bcf10";"J1305";"08";"N1101";"00"
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
          """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";2018-02-06 00:00:00.0;"28d0b75a-b694-4de3-8849-b618184bcf10";"J1305";"08";"N1101";"00"
            |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.peConnectId mustBe PEConnectId("28d0b75a-b694-4de3-8849-b618184bcf10")
      })
    }
    "integrer la date de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";2018-02-06 00:00:00.0;"28d0b75a-b694-4de3-8849-b618184bcf10";"J1305";"08";"N1101";"00"
            |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.dateEvaluation mustBe LocalDate.parse("2018-02-06"))
    }
    "integrer le code metier de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_commandepresta;dc_individu_local;dn_individu_national;dc_nom;dc_prenom;dc_telephone;dd_datecreationbeneficiaire;dc_ididentiteexterne;dc_romev3_1_id;dc_dureeexperience_1;dc_romev3_2_id;dc_dureeexperience_2
            |"H0084";"01371827194";1025354611;"ABANCOURT";"JEAN-LOUIS";"0658917580";2018-02-06 00:00:00.0;"28d0b75a-b694-4de3-8849-b618184bcf10";"J1305";"08";"N1101";"00"
            |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeMetier mustBe "H0084")
    }
  }
}
