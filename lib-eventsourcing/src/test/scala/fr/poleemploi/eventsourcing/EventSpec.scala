package fr.poleemploi.eventsourcing

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import fr.poleemploi.eventsourcing.infra.jackson.EventSourcingModule
import org.scalatest.{MustMatchers, WordSpec}

class EventSpec extends WordSpec with MustMatchers {

  object TestValueObjectModule extends EventSourcingModule {

    addStringValueObject[TestStringValueObject](classOf[TestStringValueObject], TestStringValueObject)
  }

  implicit val objectMapper: ObjectMapper =
    (new ObjectMapper() with ScalaObjectMapper)
      .registerModules(DefaultScalaModule, new JavaTimeModule, TestValueObjectModule)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDateFormat(new ISO8601DateFormat())
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)

  "fromJson" should {
    "renvoyer une erreur lorsque ce n'est pas du JSON" in {
      // Given
      val json =
        """"""

      // When & Then
      intercept[Exception] {
        Event.fromJson(json)
      }
    }
    "renvoyer une erreur lorsque le JSON est vide" in {
      // Given
      val json =
        """{}"""

      // When & Then
      intercept[Exception] {
        Event.fromJson(json)
      }
    }
    "renvoyer une erreur lorsque le JSON est valide mais que l'événement est inconnu" in {
      // Given
      val json =
        """{"champ1":"valeur"}"""

      // When & Then
      intercept[Exception] {
        Event.fromJson(json)
      }
    }
    "renvoyer un evenement lorsque le Json est valide et correspond à un événement connu" in {
      // Given
      val json =
        """{"@class":"fr.poleemploi.eventsourcing.EventTest","chaine":"hello","nombre":68,"valueObject":"test"}"""

      // When
      val result = Event.fromJson(json)

      // Then
      result.isInstanceOf[EventTest] mustBe true
      result.asInstanceOf[EventTest].chaine mustBe "hello"
      result.asInstanceOf[EventTest].nombre mustBe 68
      result.asInstanceOf[EventTest].valueObject mustBe TestStringValueObject("test")
    }
  }

  "toJson" should {
    "renvoyer une serialisation Json de l'evenement" in {
      // Given
      val event = EventTest(
        chaine = "hello",
        nombre = 43,
        valueObject = TestStringValueObject("test")
      )

      // When
      val result = Event.toJson(event)

      // Then
      result must include(""""@class":"fr.poleemploi.eventsourcing.EventTest"""")
      result must include(""""chaine":"hello"""")
      result must include(""""nombre":43""")
      result must include(""""valueObject":"test""")
      result must include(""""date":""")
    }
  }

}

case class EventTest(chaine: String,
                     nombre: Int,
                     valueObject: TestStringValueObject) extends Event

case class TestStringValueObject(value: String) extends StringValueObject
