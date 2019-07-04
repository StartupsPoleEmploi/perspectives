package fr.poleemploi.perspectives.emailing.infra.csv

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class MRSValideesProspectCandidatCSVAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val adapter: MRSValideesProspectCandidatCSVAdapter = new MRSValideesProspectCandidatCSVAdapter(actorSystem)

  "integrerMRSValidees" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nom" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le prénom" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas la date de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le statut de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le code postal" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas d'email" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de genre" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le consentement pour recevoir des mails" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un consentement négatif pour recevoir des mails" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;N;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le label du ROME de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le code ROME de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "integrer la ligne lorsqu'elle contient toutes les informations" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer le nom de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.nom.value mustBe "Nom"
      })
    }
    "integrer le prénom de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.prenom.value mustBe "Prenom"
      })
    }
    "integrer la date d'évaluation de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.dateEvaluation mustBe LocalDate.parse("2019-06-20"))
    }
    "integrer l'email de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.email.value mustBe "prenom.nom@mail.com"
      })
    }
    "integrer le genre de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.genre mustBe Genre.FEMME
      })
    }
    "integrer le code postal de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.codeDepartement mustBe CodeDepartement("69"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente une mrs validée" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.metier.codeROME mustBe CodeROME("H3404"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente l'entrée en formation du candidat (mrs validée puis entrée en formation)" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.metier.codeROME mustBe CodeROME("H3404"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente l'embauche d'un candidat (mrs validée suivie d'une embauche)" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.metier.codeROME mustBe CodeROME("H3404"))
    }
    "integrer le label du ROME de la ligne lorsqu'elle représente une mrs validée" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.metier.label mustBe "Peinture industrielle")
    }
    "integrer une seule ligne lorsque la même MRS apparait en doublon (peut avoir un statut différent avec une date de réalisation différente)" in {
      // Given
      val source = Source.single(
        ByteString(
          """dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;kc_resultatsbeneficiaire_id;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle
            |NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;VEM;69220;O;H3404;Peinture industrielle""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
  }
}
