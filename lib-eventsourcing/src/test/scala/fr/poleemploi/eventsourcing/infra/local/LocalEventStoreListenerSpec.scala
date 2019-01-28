package fr.poleemploi.eventsourcing.infra.local

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.eventsourcing.eventstore.AppendedEvent
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class LocalEventStoreListenerSpec extends AsyncWordSpec with MustMatchers
  with MockitoSugar with ScalaFutures {

  "subscribe/publish" should {
    "ne pas souscrire deux fois la même projection" in {
      // Given
      val eventStoreListener = new LocalEventStoreListener
      val appendedEvent = mockAppendedEvent
      val projection = mock[Projection]
      when(projection.onEvent)
        .thenReturn(onEventSuccess)
        .thenThrow(new RuntimeException("invocation non désirée"))
      when(projection.onEvent) thenReturn onEventError
      when(projection.listenTo) thenReturn List(classOf[Event])
      eventStoreListener.subscribe(projection)
      eventStoreListener.subscribe(projection)

      // When
      val future = eventStoreListener.publish(appendedEvent)

      // Then
      future map (_ => {
        Succeeded
      })
    }
    "ne pas bloquer la publication des autres projections si une projection échoue" in {
      // Given
      val eventStoreListener = new LocalEventStoreListener
      val appendedEvent = mockAppendedEvent
      val projection1 = mock[Projection]
      when(projection1.onEvent) thenReturn onEventError
      when(projection1.listenTo) thenReturn List(classOf[Event])
      val projection2 = mock[Projection]
      when(projection2.onEvent) thenReturn onEventSuccess
      when(projection2.listenTo) thenReturn List(classOf[Event])
      eventStoreListener.subscribe(projection1)
      eventStoreListener.subscribe(projection2)

      // When
      val future = eventStoreListener.publish(appendedEvent)

      // Then
      future map (_ => Succeeded)
    }
  }

  private def mockAppendedEvent: AppendedEvent = {
    val appendedEvent = mock[AppendedEvent]
    val event = mock[Event]
    when(appendedEvent.event) thenReturn event
    appendedEvent
  }

  private def onEventSuccess: PartialFunction[Event, Future[Unit]] = {
    case _: Event => Future.successful(())
  }

  private def onEventError: PartialFunction[Event, Future[Unit]] = {
    case _: Event => Future.failed(new RuntimeException("erreur projection"))
  }
}
