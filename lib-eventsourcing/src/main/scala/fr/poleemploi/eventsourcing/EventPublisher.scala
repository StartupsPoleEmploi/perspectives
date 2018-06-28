package fr.poleemploi.eventsourcing

import scala.collection.mutable

/**
  * Publish saved events for listeners (projections). <br />
  * It is done in the eventStore because we want to give access to the sequence number of the aggregate, in order to ensure
  * order of publication in various implementations. <br />
  * Ici encore le publisher a le choix du format pour la publication de l'event
  */
trait EventPublisher {

  def register(eventHandler: EventHandler): Unit

  def publish(appendedEvent: AppendedEvent): Unit
}

trait EventHandler {

  def handle(appendedEvent: AppendedEvent): Unit
}

/**
  * Structure de donnée n'exposant pas EventRecord mais juste les infos nécessaires
  */
case class AppendedEvent(aggregateId: AggregateId,
                         eventType: String,
                         event: Event)

class SynchronousEventPublisher extends EventPublisher {

  private val handlers: mutable.MutableList[EventHandler] = mutable.MutableList.empty

  override def register(eventHandler: EventHandler): Unit = {
    handlers += eventHandler
  }

  override def publish(appendedEvent: AppendedEvent): Unit = {
    handlers.foreach(h => h.handle(appendedEvent))
  }
}
