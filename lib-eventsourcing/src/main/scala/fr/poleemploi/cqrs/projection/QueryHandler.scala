package fr.poleemploi.cqrs.projection

trait QueryHandler

case class UnauthorizedQueryException(message: String) extends Exception(message)
