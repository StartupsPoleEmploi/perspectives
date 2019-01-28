package fr.poleemploi.perspectives.candidat.dhae.infra.csv

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.util.ByteString
import fr.poleemploi.perspectives.commun.domain.{CodeDepartement, CodeROME, Habilete}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, MustMatchers}

class HabiletesDHAECsvAdapterSpec extends AsyncWordSpec
  with MustMatchers with MockitoSugar with BeforeAndAfter with ScalaFutures {

  val actorSystem: ActorSystem = ActorSystem(this.getClass.getSimpleName)
  val habiletesDHAECsvAdapter = new HabiletesDHAECsvAdapter(actorSystem)

  "import" should {
    "ne rien faire si aucune ligne n'est présente dans la source" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future map (s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de département" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |,A1402,Aide agricole de production légumière ou végétale,Respecter des normes et des consignes,Travailler en équipe,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas de code ROME" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |72,,Aide agricole de production légumière ou végétale,Respecter des normes et des consignes,Travailler en équipe,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "ignorer la ligne si elle ne contient pas d'habiletes" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |72,A1402,Aide agricole de production légumière ou végétale,,,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => s.isEmpty mustBe true)
    }
    "integrer le code ROME de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |72,A1402,Aide agricole de production légumière ou végétale,Respecter des normes et des consignes,Travailler en équipe,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.codeROME mustBe CodeROME("A1402")
      })
    }
    "integrer les habiletés de la ligne en supprimant les espaces de début et de fin" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |72,A1402,, Respecter des normes et des consignes , Travailler en équipe ,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.habiletes must contain theSameElementsAs List(
          Habilete("Respecter des normes et des consignes"),
          Habilete("Travailler en équipe")
        )
      })
    }
    "integrer les habiletés de la ligne" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |72,A1402,Aide agricole de production légumière ou végétale,Respecter des normes et des consignes,Travailler en équipe,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.habiletes must contain theSameElementsAs List(
          Habilete("Respecter des normes et des consignes"),
          Habilete("Travailler en équipe")
        )
      })
    }
    "integrer le code departement" in {
      // Given
      val source = Source.single(
        ByteString(
          """Département,ROME commande,Appellation ROME commande,HABILETE1,HABILETE2,HABILETE3,HABILETE4,HABILETE5,HABILETE6,HABILETE7
            |72,A1402,Aide agricole de production légumière ou végétale,Respecter des normes et des consignes,Travailler en équipe,,,,,""".stripMargin)
      )

      // When
      val future = habiletesDHAECsvAdapter.load(source)

      // Then
      future.map(s => {
        s.toList.head.codeDepartement mustBe CodeDepartement("72")
      })
    }
  }
}
