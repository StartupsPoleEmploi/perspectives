package fr.poleemploi.eventsourcing

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonTypeInfo}
import com.fasterxml.jackson.databind.ObjectMapper

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonIgnoreProperties(ignoreUnknown = true)
trait Event {

}

object Event {

  def fromJson[E <: Event](data: String)(implicit objectMapper: ObjectMapper): Event =
      objectMapper.readValue(data, classOf[Event])

  def toJson(event: Event)(implicit objectMapper: ObjectMapper): String =
    objectMapper.writeValueAsString(event)
}
