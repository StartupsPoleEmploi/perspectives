package fr.poleemploi.eventsourcing

trait ValueObject

/**
  * Value Object qui contient un String
  */
trait StringValueObject extends ValueObject with StringValue

/**
  * Value Object qui contient un Int
  */
trait IntValueObject extends ValueObject with IntValue
