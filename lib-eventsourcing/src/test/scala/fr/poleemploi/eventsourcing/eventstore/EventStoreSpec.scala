package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.eventsourcing.{AggregateId, Event}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class EventStoreSpec extends AsyncWordSpec with MustMatchers
  with MockitoSugar with BeforeAndAfter with ScalaFutures {

  var eventStoreListener: EventStoreListener = _
  var appendOnlyStore: AppendOnlyStore = _
  var eventStore: EventStore = _
  var aggregateId: AggregateId = _

  before {
    eventStoreListener = mock[EventStoreListener]
    appendOnlyStore = mock[AppendOnlyStore]
    aggregateId = mock[AggregateId]
    when(aggregateId.value) thenReturn "4"

    eventStore = new EventStore(
      eventStoreListener = eventStoreListener,
      appendOnlyStore = appendOnlyStore
    )
  }

  "loadEventStream" should {
    "retourner une version à 0 lorsqu'aucun evenement n'existe" in {
      // Given
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(List())

      // When
      val future = eventStore.loadEventStream(aggregateId)

      // Then
      future map (eventStream => eventStream.version mustBe 0)
    }
    "retourner la version de l'evenement lorsqu'il existe" in {
      // Given
      val datas = mockAppendedEvents(aggregateId.value, 1)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val future = eventStore.loadEventStream(aggregateId)

      // Then
      future map (eventStream => eventStream.version mustBe 1)
    }
    "retourner la version du dernier evenement lorsque des evenements existent" in {
      // Given
      val datas = mockAppendedEvents(aggregateId.value, 7)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val future = eventStore.loadEventStream(aggregateId)

      // Then
      future map (eventStream => eventStream.version mustBe 7)
    }
    "retourner les evenements" in {
      // Given
      val datas = mockAppendedEvents(aggregateId.value, 4)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val future = eventStore.loadEventStream(aggregateId)

      // Then
      future map (eventStream => eventStream.events.size mustBe datas.size)
    }
    "retourner un stream d'evenements ordonnés" in {
      // Given
      val datas = mockAppendedEvents(aggregateId.value, 5)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val future = eventStore.loadEventStream(aggregateId)

      // Then
      future map (eventStream => eventStream.events must contain theSameElementsInOrderAs datas.map(_.event))
    }
  }

  "append" should {
    "ne pas publier un evenement s'il n'a pas pu etre sauvegarde dans le store" in {
      // Given
      when(appendOnlyStore.append(ArgumentMatchers.eq(aggregateId.value), ArgumentMatchers.eq(0), any[List[AppendOnlyData]])) thenReturn Future.failed(new RuntimeException("Erreur de sauvegarde"))

      // When & Then
      recoverToSucceededIf[RuntimeException] {
        eventStore.append(
          aggregateId = aggregateId,
          expectedVersion = 0,
          events = List(mock[Event])
        )
      } map { _ =>
        verify(eventStoreListener, never()).publish(any[AppendedEvent])
        Succeeded
      }
    }
    "sauvegarder un evenement dans le store meme si la publication au listener echoue" in {
      // Given
      when(appendOnlyStore.append(ArgumentMatchers.eq(aggregateId.value), ArgumentMatchers.eq(0), ArgumentMatchers.any[List[AppendOnlyData]])) thenReturn Future.successful()
      when(eventStoreListener.publish(ArgumentMatchers.any[AppendedEvent]())) thenReturn Future.failed(new RuntimeException("Erreur de publication"))

      // When
      val future = eventStore.append(
        aggregateId = aggregateId,
        expectedVersion = 0,
        events = List(mock[Event])
      )

      // Then
      future map (_ => Succeeded)
    }
  }

  private def mockAppendedEvents(streamName: String,
                                 size: Int): List[AppendedEvent] = {
    List.tabulate[AppendedEvent](size)(
      n => {
        val data = mock[AppendedEvent]
        when(data.streamName) thenReturn streamName
        when(data.streamVersion) thenReturn n + 1
        when(data.event) thenReturn mock[Event]
        data
      }
    )
  }
}
