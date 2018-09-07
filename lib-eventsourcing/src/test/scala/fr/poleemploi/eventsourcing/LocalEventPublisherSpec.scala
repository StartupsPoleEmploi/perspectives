package fr.poleemploi.eventsourcing

import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class LocalEventPublisherSpec extends AsyncWordSpec with MustMatchers
  with MockitoSugar with BeforeAndAfter {

  "subscribe/publish" should {
    "ne pas souscrire deux fois le même handler" in {
      // Given
      val eventPublisher = new LocalEventPublisher
      val appendedEvent = mockAppendedEvent
      val eventHandler = mock[EventHandler]
      when(eventHandler.publish(appendedEvent.event)) thenReturn Future.successful()
      eventPublisher.subscribe(eventHandler)
      eventPublisher.subscribe(eventHandler)

      // When
      val future = eventPublisher.publish(appendedEvent)

      // Then
      future map (_ => {
        verify(eventHandler, times(1)).publish(appendedEvent.event)
        Succeeded
      })
    }
    "ne pas bloquer la publication des autres handler si un handler échoue" in {
      // Given
      val eventPublisher = new LocalEventPublisher
      val appendedEvent = mockAppendedEvent
      val eventHandler1 = mock[EventHandler]
      when(eventHandler1.publish(appendedEvent.event)) thenReturn Future.failed(new RuntimeException("erreur handler1"))
      val eventHandler2 = mock[EventHandler]
      when(eventHandler2.publish(appendedEvent.event)) thenReturn Future.successful()
      eventPublisher.subscribe(eventHandler1)
      eventPublisher.subscribe(eventHandler2)

      // When
      val future = eventPublisher.publish(appendedEvent)

      // Then
      future map (_ => Succeeded)
    }
  }
  "subscribe/replay" should {
    "ne pas souscrire deux fois le même handler" in {
      // Given
      val eventPublisher = new LocalEventPublisher
      val appendedEvent = mockAppendedEvent
      val eventHandler = mock[EventHandler]
      when(eventHandler.replay(appendedEvent.event)) thenReturn Future.successful()
      eventPublisher.subscribe(eventHandler)
      eventPublisher.subscribe(eventHandler)

      // When
      val future = eventPublisher.replay(appendedEvent)

      // Then
      future map (_ => {
        verify(eventHandler, times(1)).replay(appendedEvent.event)
        Succeeded
      })
    }
    "ne pas bloquer le rejeu des autres handler si un handler échoue" in {
      // Given
      val eventPublisher = new LocalEventPublisher
      val appendedEvent = mockAppendedEvent
      val eventHandler1 = mock[EventHandler]
      when(eventHandler1.replay(appendedEvent.event)) thenReturn Future.failed(new RuntimeException("erreur handler1"))
      val eventHandler2 = mock[EventHandler]
      when(eventHandler2.replay(appendedEvent.event)) thenReturn Future.successful()
      eventPublisher.subscribe(eventHandler1)
      eventPublisher.subscribe(eventHandler2)

      // When
      val future = eventPublisher.replay(appendedEvent)

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
}
