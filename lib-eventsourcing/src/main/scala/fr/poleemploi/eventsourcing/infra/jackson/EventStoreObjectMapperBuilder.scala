package fr.poleemploi.eventsourcing.infra.jackson

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

case class EventStoreObjectMapperBuilder(stringValueObjectModule: ValueObjectModule) {

  def build(): ObjectMapper = (new ObjectMapper() with ScalaObjectMapper)
    .registerModules(DefaultScalaModule, new JavaTimeModule, stringValueObjectModule)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .setDateFormat(new ISO8601DateFormat)
    .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
    .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
}
