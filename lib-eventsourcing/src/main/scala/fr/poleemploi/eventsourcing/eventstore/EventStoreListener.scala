package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.{Event, eventSourcingLogger}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Structure de donnée n'exposant pas EventRecord mais juste les infos nécessaires aux EventStoreListener.
  */
case class AppendedEvent(streamName: String,
                         streamVersion: Int,
                         event: Event)

/**
  * Ecoute les actions qui se produisent sur l'EventStore.
  * La publication se fait dans l'EventStore, on donne accès à un AppendedEvent pour que le publisher puisse implémenter selon ces besoins (notion d'ordre avec la sequence du stream, load balancing sur le stream name, etc) <br/>
  * L'EventStoreListener a aussi le choix du format pour le rejeu de l'AppendedEvent
  */
trait EventStoreListener {

  def subscribe(projection: Projection*): Unit

  /**
    * Publie des AppendedEvent vers des Projections qui se sont préalablement enregistrées.
    */
  def publish(appendedEvent: AppendedEvent): Future[Unit]

  /**
    * Rejoue des AppendedEvent vers des Projections qui se sont préalablement enregistrées.
    */
  def replay(appendedEvent: AppendedEvent): Future[Unit]
}

/**
  * Publie les evenements directement sans les enregistrer (pourrait être fait dans une file de message par exemple)
  */
class LocalEventStoreListener extends EventStoreListener {

  private val projections: mutable.Map[Class[_ <: Event], Set[Projection]] = mutable.HashMap.empty
  private val projectionsRejouables: mutable.Map[Class[_ <: Event], Set[Projection]] = mutable.HashMap.empty

  override def subscribe(projection: Projection*): Unit =
    projection.foreach(p => p.listenTo.foreach { eventClass =>
      projections += (eventClass -> projections.getOrElse(eventClass, Set.empty).+(p))
      if (p.isReplayable)
        projectionsRejouables += (eventClass -> projectionsRejouables.getOrElse(eventClass, Set.empty).+(p))
    })

  override def publish(appendedEvent: AppendedEvent): Future[Unit] =
    sendEventToProjections(appendedEvent.event, projections)

  override def replay(appendedEvent: AppendedEvent): Future[Unit] =
    sendEventToProjections(appendedEvent.event, projectionsRejouables)

  private def sendEventToProjections(event: Event,
                                     projections: mutable.Map[Class[_ <: Event], Set[Projection]]): Future[Unit] = {
    Future.sequence(
      projections
        .filter(v => v._1 isAssignableFrom event.getClass)
        .flatMap(_._2)
        .map(p =>
          if (p.onEvent.isDefinedAt(event)) {
            p.onEvent.apply(event) recoverWith {
              case t: Throwable => Future.successful(
                if (eventSourcingLogger.isErrorEnabled) {
                  eventSourcingLogger.error(s"Erreur lors du traitement de l'evenement $event par la projection ${p.getClass.getName}", t)
                }
              )
            }
          } else Future.successful(
            if (eventSourcingLogger.isWarnEnabled) {
              eventSourcingLogger.warn(s"La projection ${p.getClass.getName} s'est enregistrée sur les evenements de type ${event.getClass.getName} mais ne les gère pas")
            }
          )
        )
    ).map(_ => ())
  }
}