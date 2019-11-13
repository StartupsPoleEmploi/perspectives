package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, IdentifiantLocal}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class MRSDHAEValideesCSVAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val mrsDHAEValideesCSVAdapter: MRSDHAEValideesCSVAdapter = new MRSDHAEValideesCSVAdapter(actorSystem)

  "integrerMRSDHAEValidees" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas l'identifiant PEConnect" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un identifiant PEConnect invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;NULL;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas l'identifiant local" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;NULL;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un identifiant local invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;NULL;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;NULL;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas la date de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le code ROME de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le label du ROME de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le site prescripteur de la MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle contient un site prescripteur invalide" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;NULL;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le nom" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le prénom" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas le code postal" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas d'email" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de genre" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "integrer la ligne lorsqu'elle contient toutes les informations" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer l'identifiant PEConnect de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.peConnectId mustBe PEConnectId("28d0b75a-b694-4de3-8849-18bfbfebd729")
      })
    }
    "integrer l'identifiant local de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.identifiantLocal mustBe IdentifiantLocal("0123456789A")
      })
    }
    "integrer la date de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.dateEvaluation mustBe LocalDate.parse("2019-02-11"))
    }
    "integrer le département issu du site prescripteur de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeDepartement mustBe CodeDepartement("85"))
    }
    "integrer le code ROME de la ligne lorsqu'elle représente une MRS" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.metier.codeROME mustBe CodeROME("H2102"))
    }
    "integrer une seule ligne lorsque la même MRS apparait en doublon (peut avoir une date de modification différente)" in {
      // Given
      val source = Source.single(
        ByteString(
          """kn_individu_national;dc_individu_local;dc_nom;dc_prenom;dc_sexe_id;dc_codepostal;dc_adresseemail;kc_action_prestation_id;dd_datedebutprestation;dc_uniteprescriptrice;dc_ididentiteexterne;kd_datemodification;dc_rome_id;dc_lblrome
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-03-07 20:52:33;H2102;Peinture industrielle
            |1208342958;0123456789A;NOM;PRENOM;F;85000;PRENOM.NOM@mail.com;P50;2019-02-11 00:00:00;85012;28d0b75a-b694-4de3-8849-18bfbfebd729;2019-05-08 11:24:49;H2102;Peinture industrielle""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
  }
}
