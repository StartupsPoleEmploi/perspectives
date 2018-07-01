package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.eventsourcing.{AggregateId, AppendedEvent, Event, EventPublisher}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Class for high-level data access to the store. <br />
  * Handle concurrency exception and publish events.
  */
class EventStore(eventPublisher: EventPublisher,
                 appendOnlyStore: AppendOnlyStore) {

  /**
    * Load the EventStream corresponding to an aggregate
    *
    * @param aggregateId id of the aggregate
    * @return
    */
  def loadEventStream(aggregateId: AggregateId): Future[EventStream] =
    appendOnlyStore.readRecords(aggregateId.value).map { datas =>
      datas.foldRight(EventStream(0, Nil))((data, es) =>
        EventStream(if (es.version < data.streamVersion) data.streamVersion else es.version, data.event :: es.events)
      )
    }

  /**
    * Appends events for the provided aggregate.
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
      events.foreach(e => {
        eventPublisher.publish(
          AppendedEvent(
            aggregateId = AggregateId(aggregateId.value),
            eventType = e.getClass.getSimpleName,
            event = e
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
                                  actualStreamVersion: Int, events: List[Event]): Future[Unit] = {
    Future.successful(println(s"CONFLIT SUR AGGREGAT id $id. expectedStreamVersion : $expectedStreamVersion, actualStreamVersion : $actualStreamVersion"))
  }
}

case class EventStream(version: Int, events: List[Event])

/**
  * Is supposed to be thrown by the client code, when it fails to resolve concurrency problem
  */
case class EventStoreConcurrencyException(message: String) extends Exception(message)
