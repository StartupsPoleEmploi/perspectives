package fr.poleemploi.cqrs.projection

import fr.poleemploi.eventsourcing.{AggregateId, Event}

import scala.concurrent.Future

/**
  * Ecoute des événements et permet de mettre à jour une représentation de ceux-ci. <br/>
  * Peut-etre rejouée (ex: un index Elasticsearch ou une table en BDD) ou non (ex: envoi d'un email). <br />
  * <strong>Ne pas faire de jointures et avoir les informations prêtes dans la projection : la dénormalisation n'est pas un problème ici</strong>
*/
trait Projection {

  type ReceiveEvent = PartialFunction[Event, Future[Unit]]

  /**
    * @return La liste des classes d'evenement à écouter
    */
  def listenTo: List[Class[_ <: Event]]

  /**
    * Appelée lors de la réception d'un événement écouté par la projection.
    *
    * @return ReceiveEvent: le Future[Unit] indiquant un side effect sur la projection
    */
  def onEvent(aggregateId: AggregateId): ReceiveEvent

  /**
    * Indique si la projection peut être rejouée
    */
  def isReplayable: Boolean
}
