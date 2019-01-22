package fr.poleemploi.perspectives.commun.infra.play.cache

import akka.Done
import play.api.cache.AsyncCacheApi

import scala.collection.mutable
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

import scala.concurrent.ExecutionContext.Implicits.global

class InMemoryCacheApi extends AsyncCacheApi {

  private val cache: mutable.Map[String, Any] = new mutable.HashMap[String, Any].empty

  override def set(key: String, value: Any, expiration: Duration): Future[Done] =
    Future(cache.put(key, value)).map(_ => Done)

  override def remove(key: String): Future[Done] =
    Future(cache.remove(key)).map(_ => Done)

  override def getOrElseUpdate[A](key: String, expiration: Duration)(orElse: => Future[A])(implicit evidence$1: ClassTag[A]): Future[A] =
    cache.get(key).map(v => Future(v.asInstanceOf[A])).getOrElse(orElse.map(v => {
      cache.put(key, v)
      v
    }))

  override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Future[Option[T]] =
    Future(cache.get(key).map(_.asInstanceOf[T]))

  override def removeAll(): Future[Done] = Future {
    cache.clear()
    Done
  }
}
