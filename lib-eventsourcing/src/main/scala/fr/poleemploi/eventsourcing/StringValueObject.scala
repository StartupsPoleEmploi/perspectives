package fr.poleemploi.eventsourcing

trait ValueObject

/**
  * Value Object qui wrappe un String
  */
trait StringValueObject extends ValueObject {

  def value: String
}

/**
  * Value Object qui wrappe un Int
  */
trait IntValueObject extends ValueObject {

  def value: Int
}
