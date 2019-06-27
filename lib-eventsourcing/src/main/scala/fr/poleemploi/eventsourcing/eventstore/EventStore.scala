package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.eventsourcing._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Class for high-level data access to the event store. <br />
  * Handle concurrency exception and publish events.
  */
class EventStore(eventStoreListener: EventStoreListener,
                 appendOnlyStore: AppendOnlyStore) {

  /**
    * Load the EventStream corresponding to an aggregate
    *
    * @param aggregateId id of the aggregate
    * @return
    */
  def loadEventStream(aggregateId: AggregateId): Future[EventStream] =
    appendOnlyStore
      .readRecords(streamName = aggregateId.value, version = None)
      .map(appendedEvents => buildEventStream(appendedEvents = appendedEvents, version = 0))

  /**
    * Load the EventStream corresponding to an aggregate after a specific version
    *
    * @param aggregateId id of the aggregate
    * @param version Version of the aggregate
    * @return
    */
  def loadEventStreamAfterVersion(aggregateId: AggregateId, version: Int): Future[EventStream] =
    appendOnlyStore
      .readRecords(streamName = aggregateId.value, version = Some(version))
      .map(appendedEvents => buildEventStream(appendedEvents = appendedEvents, version = version))

  /**
    * Append events for the provided aggregate.
    *
    * @param aggregateId     aggregate id to append to.
    * @param expectedVersion The expected version that will be checked for concurrency.
    * @param events          The events to append.
    * @return The new aggregate version or a ConcurrencyException when the used EventConflictResolutionStrategy has failed to resolve a conflict
    * @throws EventStoreConcurrencyException if new events were appended since expectedVersion and conflict cannot be resolved
    */
  def append(aggregateId: AggregateId,
             expectedVersion: Int,
             events: List[Event]): Future[Unit] = {
    val datas = events.zip(Stream.from(expectedVersion + 1)).map {
      case (e, v) => AppendOnlyData(
        eventType = e.getClass.getSimpleName,
        streamVersion = v,
        event = e
      )
    }

    appendOnlyStore.append(
      streamName = aggregateId.value,
      expectedStreamVersion = expectedVersion,
      datas = datas
    ).map { _ =>
      datas.foreach(a => {
        // On attend pas le retour de la publication
        eventStoreListener.publish(
          AppendedEvent(
            streamName = aggregateId.value,
            streamVersion = a.streamVersion,
            event = a.event
          )
        )
      })
    }.recoverWith {
      case ex: AppendOnlyStoreConcurrencyException =>
        tryResolveConflicts(aggregateId, ex.expectedStreamVersion, ex.actualStreamVersion, events)
    }
  }

  // TODO
  private def tryResolveConflicts(id: AggregateId,
                                  expectedStreamVersion: Int,
                                  actualStreamVersion: Int,
                                  events: List[Event]): Future[Unit] = {
    Future.successful(
      if (eventSourcingLogger.isErrorEnabled) {
        eventSourcingLogger.warn(s"Conflit sur l'aggregat ${id.value}. expectedStreamVersion : $expectedStreamVersion, actualStreamVersion : $actualStreamVersion. Events non insérés : $events")
      }
    )
  }

  private def buildEventStream(appendedEvents: List[AppendedEvent], version: Int): EventStream =
    appendedEvents.foldRight(EventStream(version, Nil))((ae, es) =>
      EventStream(
        version = if (es.version < ae.streamVersion) ae.streamVersion else es.version,
        events = ae.event :: es.events
      )
    )
}

case class EventStream(version: Int, events: List[Event])

/**
  * Is supposed to be thrown by the client code, when it fails to resolve concurrency problem
  */
case class EventStoreConcurrencyException(message: String) extends Exception(message)
