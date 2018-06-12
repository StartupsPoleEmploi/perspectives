package fr.poleemploi.eventsourcing

import java.time.ZonedDateTime

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import org.scalatest.{MustMatchers, WordSpec}

class EventSpec extends WordSpec with MustMatchers {

  implicit val objectMapper: ObjectMapper =
    (new ObjectMapper() with ScalaObjectMapper)
      .registerModules(DefaultScalaModule, new JavaTimeModule)
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
        """{"@class":"fr.poleemploi.eventsourcing.EventTest","chaine":"hello","nombre":68}"""

      // When
      val result = Event.fromJson(json)

      // Then
      result.isInstanceOf[EventTest] mustBe true
      result.asInstanceOf[EventTest].chaine mustBe "hello"
      result.asInstanceOf[EventTest].nombre mustBe 68
    }
  }

  "toJson" should {
    "renvoyer une serialisation Json de l'evenement" in {
      // Given
      val event = EventTest(
        chaine = "hello",
        nombre = 43
      )

      // When
      val result = Event.toJson(event)

      // Then
      result mustBe """{"@class":"fr.poleemploi.eventsourcing.EventTest","chaine":"hello","nombre":43}"""
    }
    "renvoyer une serialisation Json d'un evenement contenant une date" in {
      // Given
      val event = EventWithDate(
        date = ZonedDateTime.now()
      )

      // When
      val result = Event.toJson(event)

      // Then
      result must include(""""date":""")
    }
  }

}

case class EventTest(chaine: String,
                     nombre: Int) extends Event

case class EventWithDate(date: ZonedDateTime) extends Event
