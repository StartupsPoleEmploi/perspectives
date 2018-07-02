package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.eventsourcing.Event

import scala.concurrent.Future

/**
  * Base class for low-level data access. <br />
  * To be implemented by various storage engines such as SQL, NoSQL, Documents, etc.
  */
trait AppendOnlyStore {

  /**
    * Appends events to the stream with the specified name. <br />
    * If expectedStreamVersion does not match the last stored version, an AppendOnlyStoreConcurrencyException is thrown.
    *
    * @param streamName Identifier of the stream
    * @param expectedStreamVersion The expected version of the stream that will be checked for concurrency.
    * @param datas List of <code>AppendOnlyData</code> to append
    * @throws fr.poleemploi.eventsourcing.eventstore.AppendOnlyStoreConcurrencyException when expected stream version does not match the last stored version
    */
  def append(streamName: String,
             expectedStreamVersion: Int,
             datas: List[AppendOnlyData]): Future[Unit]

  /**
    * Load records for a streamName.
    *
    * @param streamName Identifier of the stream
    * @return List of <code>AppendOnlyData</code> : empty when there is no records
    */
  def readRecords(streamName: String): Future[List[AppendOnlyData]]
}

/**
  * Structure representing what is to be persisted by the AppendOnlyStore
  */
case class AppendOnlyData(streamVersion: Int,
                          eventType: String,
                          event: Event)

/**
  * Is thrown internally, when storage version does not match the condition specified in server request
  *
  * @param expectedStreamVersion expected stream version
  * @param actualStreamVersion   actual stream version
  * @param streamName            stream name
  */
case class AppendOnlyStoreConcurrencyException(expectedStreamVersion: Int,
                                               actualStreamVersion: Int,
                                               streamName: String)
  extends Exception(s"Expected version $expectedStreamVersion in stream '$streamName' but got $actualStreamVersion")