package fr.poleemploi.cqrs.projection

case class QueryException(cause: Throwable)  extends Exception(cause)