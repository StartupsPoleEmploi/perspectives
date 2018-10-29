package fr.poleemploi.cqrs.projection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait QueryHandler {

  def configure: PartialFunction[Query[_ <: QueryResult], Future[QueryResult]]

  def handle[R <: QueryResult](query: Query[R]): Future[R] =
    if (configure.isDefinedAt(query))
      configure.apply(query).map(_.asInstanceOf[R])
    else
      Future.failed(new IllegalArgumentException(s"Aucun handler n'est défini pour exécuter la commande $query"))
}

case class UnauthorizedQueryException(message: String) extends Exception(message)