package fr.poleemploi.perspectives.emailing.infra.csv

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class OffresGereesParRecruteurCSVAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val adapter: OffresGereesParRecruteurCSVAdapter = new OffresGereesParRecruteurCSVAdapter(actorSystem)

  "importerOffres" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas l'id de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
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
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de code postal valide" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;123456789;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de code postal cible" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;19100;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le mail du recruteur" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un mail de recruteur null" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;null""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nom de l'enseigne qui recrute" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nom du recruteur" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le code ROME de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nom de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;;GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le lieu de travail de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);;32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
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
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer l'id de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.offreId.value mustBe "094DLLY")
    }
    "integrer le nom de l'enseigne" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.enseigne mustBe "ENTREPRISE")
    }
    "integrer le mail du recruteur" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.emailCorrespondant.value mustBe "test@email.fr")
    }
    "integrer le nom du recruteur" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.nomCorrespondant mustBe "RH ENTREPRISE")
    }
    "integrer le code postal" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.codePostal.value mustBe "50400")
    }
    "integrer le code ROME" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.codeROME.value mustBe "H3101")
    }
    "integrer le titre de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.intitule mustBe "Conducteur / Conductrice de machines à onduler (H/F)")
    }
    "integrer le lieu de travail de l'offre" in {
      // Given
      val source = Source.single(
        ByteString(
          """preselection_deduite;kc_offre;dd_datecreationreport;dc_rome_id;intitule;lieu_de_travail;siret;enseigne;code_postal;nom_prenom_correspondant_offre;mail_suivi
            |non;094DLLY;2019-10-01;H3101;Conducteur / Conductrice de machines à onduler (H/F);GRANVILLE (50);32992501005763;ENTREPRISE;50400;RH ENTREPRISE;test@email.fr""".stripMargin)
      )

      // When
      val future = adapter.load(source)

      // Then
      future.map(s => s.toList.head.lieuTravail mustBe "GRANVILLE (50)")
    }
  }
}
