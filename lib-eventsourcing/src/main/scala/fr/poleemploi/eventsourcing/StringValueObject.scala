package fr.poleemploi.eventsourcing

trait ValueObject

/**
  * Value Object qui wrappe un String
  */
trait StringValueObject extends ValueObject {

  def value: String
}
