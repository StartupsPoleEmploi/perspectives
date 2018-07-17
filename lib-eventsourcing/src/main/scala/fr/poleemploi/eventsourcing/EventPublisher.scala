package fr.poleemploi.eventsourcing

import fr.poleemploi.cqrs.projection.Projection

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Structure de donnée n'exposant pas EventRecord mais juste les infos nécessaires aux EventPublisher.
  */
case class AppendedEvent(aggregateId: AggregateId,
                         event: Event)

/**
  * Publie des AppendEvent vers des listeners. (EventHandler)
  * La publication se fait dans l'EventStore, on donne accès à un AppendedEvent pour que le publisher puisse implémenter selon ces besoins (notion d'ordre avec la sequence du stream, load balancing sur le stream name, etc) <br/>
  * L'EventPublisher publisher a aussi le choix du format pour la publication de l'event
  */
trait EventPublisher {

  def subscribe(eventHandler: EventHandler): Unit

  def publish(publishedEvent: AppendedEvent): Future[Unit]
}

/**
  * Publie et rejoue des Event vers des Projection.
  */
trait EventHandler {

  def subscribe(projection: Projection*): Unit

  def publish(aggregateId: AggregateId, event: Event): Future[Unit]

  def replay(aggregateId: AggregateId, event: Event): Future[Unit]
}

/**
  * Publie les evenements directement sans les enregistrer (dans une file de message par exemple)
  */
class LocalEventPublisher extends EventPublisher {

  private val handlers: mutable.MutableList[EventHandler] = mutable.MutableList.empty

  override def subscribe(eventHandler: EventHandler): Unit = {
    handlers += eventHandler
  }

  override def publish(publishedEvent: AppendedEvent): Future[Unit] =
    Future.sequence(
      handlers.map(h => h.publish(publishedEvent.aggregateId, publishedEvent.event))
    ).map(_ => ())
}

class LocalEventHandler extends EventHandler {

  private val publishHandlers: mutable.Map[Class[_ <: Event], Set[Projection]] = mutable.HashMap.empty
  private val replayHandlers: mutable.Map[Class[_ <: Event], Set[Projection]] = mutable.HashMap.empty

  override def subscribe(projection: Projection*): Unit =
    projection.foreach(p => p.listenTo.foreach { eventClass =>
      publishHandlers += (eventClass -> publishHandlers.getOrElse(eventClass, Set.empty).+(p))
      if (p.isReplayable)
        replayHandlers += (eventClass -> publishHandlers.getOrElse(eventClass, Set.empty).+(p))
    })

  override def publish(aggregateId: AggregateId, event: Event): Future[Unit] =
    sendEventToProjections(aggregateId, event, publishHandlers)

  override def replay(aggregateId: AggregateId, event: Event): Future[Unit] =
    sendEventToProjections(aggregateId, event, replayHandlers)

  private def sendEventToProjections(aggregateId: AggregateId,
                                     event: Event,
                                     handlers: mutable.Map[Class[_ <: Event], Set[Projection]]): Future[Unit] = {
    Future.sequence(
      handlers
        .filter(v => v._1 isAssignableFrom event.getClass)
        .flatMap(_._2)
        .map(p =>
          if (p.onEvent(aggregateId).isDefinedAt(event)) {
            p.onEvent(aggregateId).apply(event) recoverWith {
              case t: Throwable => Future.successful(println(s"Erreur lors de la publication de l'evenement $event par la projection ${p.getClass.getName}. ${t.getCause} : ${t.getMessage}"))
            }
          } else Future.successful(println(s"La projection ${p.getClass.getName} s'est enregistrée sur les evenements de type ${event.getClass.getName} mais ne les gère pas"))
        )
    ).map(_ => ())
  }
}