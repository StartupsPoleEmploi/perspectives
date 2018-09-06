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
        """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur""".stripMargin)
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
        """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
          |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,,44210 PORNIC,44021
          |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,NULL,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,,VSL,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VSL,SELECTIONNE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VEF,ENTREE EN FORMATION,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
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
          """KN_IDCOMMANDEPRESTA,KN_COMMANDEBENEFICIAIRE,DN_INDIVIDU_NATIONAL,DC_ROME_ID,DC_NOM,DC_PRENOM,DD_DATESORTIEPRESTATIONPREVUE,KC_RESULTATSBENEFICIAIRE_ID,DC_LBLRESULTATBENEFICIAIRE,DC_TOPSORTIEANTICIPEPOSITIVE,C_ROME_1_ID,C_ROME_2_ID,Identifiant PE connect,Ville habitation DE,Site Prescripteur
            |5717975,19859698,1234556789,H2909,NOM,PRENOM,2018-01-09 00:00:00.000,VEM,EMBAUCHE,N,K1302,K1302,28d0b75a-b694-4de3-8849-18bfbfebd729,44210 PORNIC,44021
            |""".stripMargin)
      )

      // When
      val future = mrsValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeROME mustBe CodeROME("H2909"))
    }
  }
}
