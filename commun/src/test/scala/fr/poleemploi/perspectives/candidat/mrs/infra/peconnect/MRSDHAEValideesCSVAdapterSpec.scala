package fr.poleemploi.perspectives.candidat.mrs.infra.peconnect

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME}
import fr.poleemploi.perspectives.commun.infra.peconnect.PEConnectId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class MRSDHAEValideesCSVAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val mrsDHAEValideesCSVAdapter: MRSDHAEValideesCSVAdapter = new MRSDHAEValideesCSVAdapter(actorSystem)

  "integrerMRSValidees" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas d'identité externe" in {
      // Given
      val source = Source.single(
        ByteString(
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"||2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|NULL|2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50||"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00||"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|NULL|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
    "integrer l'identifiant externe de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.peConnectId mustBe PEConnectId("28d0b75a-b694-4de3-8849-18bfbfebd729")
      })
    }
    "integrer la date de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
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
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.toList.head.codeROME mustBe CodeROME("H2102"))
    }
    "integrer une seule ligne lorsque la même MRS apparait en doublon (peut avoir une date de modification différente)" in {
      // Given
      val source = Source.single(
        ByteString(
          """"a.kn_individu_national"|"a.dc_individu_local"|"a.kc_action_prestation_id"|"a.dd_datedebutprestation"|"a.dc_uniteprescriptrice"|"b.dc_ididentiteexterne"|"c.kd_datemodification"|"c.dc_rome_id"
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-03-07 20:52:33|H2102
            |1208342958|"01341957989"|P50|2019-02-11 00:00:00|"85012"|"28d0b75a-b694-4de3-8849-18bfbfebd729"|2019-05-08 20:52:33|H2102""".stripMargin)
      )

      // When
      val future = mrsDHAEValideesCSVAdapter.load(source)

      // Then
      future.map(s => s.size mustBe 1)
    }
  }
}
