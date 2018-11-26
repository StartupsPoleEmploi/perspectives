package fr.poleemploi.eventsourcing.infra.akka

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification
import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.eventSourcingLogger
import fr.poleemploi.eventsourcing.eventstore.AppendedEvent
import fr.poleemploi.eventsourcing.infra.akka.ProjectionManagerActor.Publish

class ProjectionEventBus(projectionManagerActor: ActorRef) extends EventBus with SubchannelClassification {

  override type Event = AppendedEvent
  override type Classifier = Class[_]
  override type Subscriber = Projection

  override protected implicit val subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y

    def isSubclass(x: Classifier, y: Classifier): Boolean = y isAssignableFrom x
  }

  override protected def publish(appendedEvent: Event, projection: Subscriber): Unit = {
    if (projection.onEvent.isDefinedAt(appendedEvent.event)) {
      projectionManagerActor ! Publish(projection = projection, appendedEvent = appendedEvent)
    } else if (eventSourcingLogger.isWarnEnabled) {
      eventSourcingLogger.warn(s"La projection ${projection.getClass.getName} s'est enregistrée sur les evenements de type ${appendedEvent.event.getClass.getName} mais ne les gère pas")
    }
  }

  override protected def classify(appendedEvent: Event): Classifier = appendedEvent.event.getClass
}
