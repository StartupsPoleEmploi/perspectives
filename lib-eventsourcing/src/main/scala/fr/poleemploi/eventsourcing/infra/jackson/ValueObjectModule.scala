package fr.poleemploi.eventsourcing.infra.jackson

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import fr.poleemploi.eventsourcing.StringValueObject

trait ValueObjectModule extends SimpleModule {

  def addStringValueObject[T <: StringValueObject](clazz: Class[T],
                                                   deserialize: String => T): SimpleModule = {
    addSerializer(clazz, new StringValueSerializer[T](clazz))
    addDeserializer(clazz, new StringValueDeserializer[T](clazz, deserialize))
  }
}

class StringValueSerializer[T <: StringValueObject](clazz: Class[T]) extends StdScalarSerializer[T](clazz) {

  override def serialize(t: T, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit =
    jsonGenerator.writeString(t.value)
}

class StringValueDeserializer[T <: StringValueObject](clazz: Class[T], deserialize: String => T) extends StdScalarDeserializer[T](clazz) {

  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): T = {
    val currentToken = jsonParser.getCurrentToken
    if (currentToken == JsonToken.VALUE_STRING) deserialize(jsonParser.getText.trim())
    else throw deserializationContext.mappingException("Expected value String")
  }

}