package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
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
  "subscribe/replay" should {
    "ne pas publier lorsque la projection n'est pas rejouable" in {
      // Given
      val eventStoreListener = new LocalEventStoreListener
      val appendedEvent = mockAppendedEvent
      val projection = mock[Projection]
      when(projection.onEvent) thenThrow new RuntimeException("invocation indésirée")
      when(projection.listenTo) thenReturn List(classOf[Event])
      when(projection.isReplayable) thenReturn false
      eventStoreListener.subscribe(projection)

      // When
      val future = eventStoreListener.replay(appendedEvent)

      // Then
      future map (_ => {
        Succeeded
      })
    }
    "ne pas souscrire deux fois la même projection" in {
      // Given
      val eventStoreListener = new LocalEventStoreListener
      val appendedEvent = mockAppendedEvent
      val projection = mock[Projection]
      when(projection.onEvent)
        .thenReturn(onEventSuccess)
        .thenReturn(onEventSuccess)
        .thenThrow(new RuntimeException("invocation non désirée"))
      when(projection.listenTo) thenReturn List(classOf[Event])
      when(projection.isReplayable) thenReturn true
      eventStoreListener.subscribe(projection)
      eventStoreListener.subscribe(projection)

      // When
      val future = eventStoreListener.replay(appendedEvent)

      // Then
      future map (_ => {
        Succeeded
      })
    }
    "ne pas bloquer le rejeu des autres projection si une projection échoue" in {
      // Given
      val eventStoreListener = new LocalEventStoreListener
      val appendedEvent = mockAppendedEvent
      val projection1 = mock[Projection]
      when(projection1.onEvent) thenReturn onEventError
      when(projection1.listenTo) thenReturn List(classOf[Event])
      when(projection1.isReplayable) thenReturn true
      val projection2 = mock[Projection]
      when(projection2.onEvent) thenReturn onEventSuccess
      when(projection2.listenTo) thenReturn List(classOf[Event])
      when(projection2.isReplayable) thenReturn true
      eventStoreListener.subscribe(projection1)
      eventStoreListener.subscribe(projection2)

      // When
      val future = eventStoreListener.replay(appendedEvent)

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
    case _: Event => Future.successful()
  }

  private def onEventError: PartialFunction[Event, Future[Unit]] = {
    case _: Event => Future.failed(new RuntimeException("erreur projection"))
  }
}
