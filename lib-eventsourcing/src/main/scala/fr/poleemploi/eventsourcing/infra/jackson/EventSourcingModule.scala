package fr.poleemploi.eventsourcing.infra.jackson

import com.fasterxml.jackson.core.{JsonGenerator, JsonParser, JsonToken}
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import com.fasterxml.jackson.databind.{DeserializationContext, SerializerProvider}
import fr.poleemploi.eventsourcing._

trait EventSourcingModule extends SimpleModule {

  def addAggregateId[T <: AggregateId](clazz: Class[T],
                                       deserialize: String => T): Unit = {
    addSerializer(clazz, new StringValueSerializer[T](clazz))
    addDeserializer(clazz, new StringValueDeserializer[T](clazz, deserialize))
  }

  def addEntityId[T <: EntityId](clazz: Class[T],
                                 deserialize: String => T): Unit = {
    addSerializer(clazz, new StringValueSerializer[T](clazz))
    addDeserializer(clazz, new StringValueDeserializer[T](clazz, deserialize))
  }

  def addStringValueObject[T <: StringValueObject](clazz: Class[T],
                                                   deserialize: String => T): Unit = {
    addSerializer(clazz, new StringValueSerializer[T](clazz))
    addDeserializer(clazz, new StringValueDeserializer[T](clazz, deserialize))
  }

  def addIntValueObject[T <: IntValueObject](clazz: Class[T],
                                             deserialize: Int => T): Unit = {
    addSerializer(clazz, new IntValueSerializer[T](clazz))
    addDeserializer(clazz, new IntValueDeserializer[T](clazz, deserialize))
  }
}

class StringValueSerializer[T <: StringValue](clazz: Class[T]) extends StdScalarSerializer[T](clazz) {

  override def serialize(t: T, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit =
    jsonGenerator.writeString(t.value)
}

class StringValueDeserializer[T <: StringValue](clazz: Class[T], deserialize: String => T) extends StdScalarDeserializer[T](clazz) {

  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): T = {
    val currentToken = jsonParser.getCurrentToken
    if (currentToken == JsonToken.VALUE_STRING) deserialize(jsonParser.getText.trim())
    else throw deserializationContext.mappingException("Expected value String")
  }

}

class IntValueSerializer[T <: IntValue](clazz: Class[T]) extends StdScalarSerializer[T](clazz) {

  override def serialize(t: T, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider): Unit =
    jsonGenerator.writeNumber(t.value)
}

class IntValueDeserializer[T <: IntValue](clazz: Class[T], deserialize: Int => T) extends StdScalarDeserializer[T](clazz) {

  override def deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): T = {
    val currentToken = jsonParser.getCurrentToken
    if (currentToken == JsonToken.VALUE_NUMBER_INT) deserialize(jsonParser.getIntValue)
    else throw deserializationContext.mappingException("Expected value Int")
  }

}