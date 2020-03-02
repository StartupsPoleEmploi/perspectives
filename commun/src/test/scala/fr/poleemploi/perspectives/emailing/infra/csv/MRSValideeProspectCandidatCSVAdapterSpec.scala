package fr.poleemploi.perspectives.emailing.infra.csv

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain._
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class MRSValideeProspectCandidatCSVAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val adapter: MRSValideeProspectCandidatCSVAdapter = new MRSValideeProspectCandidatCSVAdapter(actorSystem)

  "integrerMRSValidees" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas l'identifiant PEConnect" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un identifiant PEConnect invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |NULL;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas l'identifiant local" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un identifiant local invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;NULL;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }

    "ignorer la ligne si elle ne contient pas le code neptune" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un code neptune invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;NULL;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nom" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de resultat MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un resultat MRS invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;XXX""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;N;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer l'identifiant PEConnect de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.peConnectId mustBe PEConnectId("28d0b75a-b694-4de3-8849-18bfbfebd729")
      })
    }
    "integrer l'identifiant local de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.identifiantLocal mustBe IdentifiantLocal("0123456789A")
      })
    }
    "integrer le code neptune de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.codeNeptune mustBe CodeNeptune("IDMU6650")
      })
    }
    "integrer le nom de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.email.value mustBe "prenom.nom@mail.com"
      })
    }
    "integrer le resultat MRS de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.resultatMrs.get mustBe ResultatMrs.VSL
      })
    }
    "integrer le genre de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
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
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.codeDepartement mustBe CodeDepartement("69"))
    }
    "integrer le code ROME de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.metier.codeROME mustBe CodeROME("H3404"))
    }
    "integrer la MRS la plus récente du prospect lorsque deux MRS apparaissent avec des métiers différents" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-21 00:00:00;69220;O;H2909;Montage-assemblage mécanique;VEM
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.size mustBe 1
        s.toList.head.metier.codeROME mustBe CodeROME("H2909")
      })
    }
    "integrer la MRS la plus récente du prospect lorsque deux MRS apparaissent avec les mêmes métiers et des dates différentes" in {
      // Given
      val source = Source.single(
        ByteString(
          """id_peconnect;identifiant_local;dc_agentpe_referent_id;dc_nom;dc_prenom;dc_adresseemail;dc_sexe_id;dd_daterealisation;dc_codepostal;dc_consentement_mail_id;dc_rome_id;dc_lblrome;resultatsbeneficiaire
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-20 00:00:00;69220;O;H3404;Peinture industrielle
            |28d0b75a-b694-4de3-8849-18bfbfebd729;0123456789A;IDMU6650;NOM;PRENOM;PRENOM.NOM@mail.com;F;2019-06-21 00:00:00;69220;O;H3404;Peinture industrielle;VSL""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => {
        s.size mustBe 1
        s.toList.head.dateEvaluation mustBe LocalDate.parse("2019-06-21")
      })
    }
  }
}
