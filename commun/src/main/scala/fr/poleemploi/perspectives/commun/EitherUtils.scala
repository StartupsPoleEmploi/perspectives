package fr.poleemploi.perspectives.commun

import scala.concurrent.Future

object EitherUtils {

  implicit class EitherToFuture[T](e: Either[String, T]) {

    def toFuture: Future[T] =
      e match {
        case Left(erreur) => Future.failed(new IllegalArgumentException(erreur))
        case Right(value) => Future.successful(value)
      }
  }
}
