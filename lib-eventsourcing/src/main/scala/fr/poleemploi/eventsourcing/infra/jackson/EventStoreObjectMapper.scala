package fr.poleemploi.eventsourcing.infra.jackson

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object EventStoreObjectMapper {

  val mapper: ObjectMapper = (new ObjectMapper() with ScalaObjectMapper)
    .registerModules(DefaultScalaModule)
    .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
    .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
}
