package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event

import scala.concurrent.Future

/**
  * Structure de donnée n'exposant pas EventRecord mais juste les infos nécessaires aux EventStoreListener.
  */
case class AppendedEvent(streamName: String,
                         streamVersion: Int,
                         event: Event)

/**
  * Ecoute les actions qui se produisent sur l'EventStore.
  * La publication se fait dans l'EventStore, on donne accès à un AppendedEvent pour que l'EventStoreListener puisse implémenter selon ces besoins (notion d'ordre avec la sequence du stream, load balancing sur le stream name, etc) <br/>
  * L'EventStoreListener est responsable de la stratégie utilisée pour envoyer les événements aux projections
  */
trait EventStoreListener {

  def subscribe(projections: Projection*): Unit

  /**
    * Publie des AppendedEvent vers des Projections qui se sont préalablement enregistrées.
    */
  def publish(appendedEvent: AppendedEvent): Future[Unit]
}