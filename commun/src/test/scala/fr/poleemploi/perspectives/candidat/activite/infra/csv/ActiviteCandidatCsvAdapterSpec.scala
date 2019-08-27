package fr.poleemploi.perspectives.candidat.activite.infra.csv

import java.time.Month

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class ActiviteCandidatCsvAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val activiteCandidatCsvAdapter = new ActiviteCandidatCSVAdapter(actorSystem)

  "import" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas d'identité externe" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;;;;;;""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un identifiant PEConnect invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;NULL;;;;;""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nombre d'heures travaillees" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;NULL;;;;;""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un nombre d'heures travaillees invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;NULL;;;;;NULL""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas l'annee d'actualisation" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;NULL;;;;;""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient une annee d'actualisation invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;224ac416-35b0-430b-9a0c-eab8d631cb69;NULL;;;;""".stripMargin)
        )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le mois d'actualisation" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;;;;;""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un mois d'actualisation invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;;13;;;""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "integrer un seul candidat quand doublon d'identifiant externe" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
             1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;19;5;;;114
             1295807129;0011269545R;FLAN;CHRISTELLE;0631615537;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;19;5;;;18""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer la ligne lorsqu'elle contient toutes les informations" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;19;5;;;114""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer l'identifiant externe de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;19;5;;;114""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.peConnectId mustBe PEConnectId("224ac416-35b0-430b-9a0c-eab8d631cb69")
      })
    }
    "integrer le nombre d'heures travaillees de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;19;5;;;114""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.nbHeuresTravaillees mustBe 114
      })
    }
    "integrer la date d'actualisation de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_telephone;dc_codealeinscription;dc_ididentiteexterne;kc_anneeactualisation;kc_moisactualisation;travail_decla;salaire_brut_decla_actu;nb_heure_travail_decla_actu
            |1299634763;0011152727P;FLAN;CHRISTELLE;0679247212;64046;224ac416-35b0-430b-9a0c-eab8d631cb69;19;5;;;114""".stripMargin)
      )

      // When
      val future = activiteCandidatCsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.dateActualisation.getYear mustBe 2019
        s.toList.head.dateActualisation.getMonth mustBe Month.MAY
        s.toList.head.dateActualisation.getDayOfMonth mustBe 1
      })
    }
  }
}
