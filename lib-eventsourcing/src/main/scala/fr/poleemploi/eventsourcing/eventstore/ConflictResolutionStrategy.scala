package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.eventsourcing.Event

trait ConflictResolutionStrategy {

  def conflictsWith(failedEvent: Event, succeededEvent: Event): Boolean
}

object NoConflictResolutionStrategy extends ConflictResolutionStrategy {

  override def conflictsWith(failedEvent: Event, succeededEvent: Event): Boolean = true
}

object EventTypeConflictResolutionStrategy extends ConflictResolutionStrategy {

  override def conflictsWith(failedEvent: Event, succeededEvent: Event): Boolean =
    failedEvent.getClass == succeededEvent.getClass
}
