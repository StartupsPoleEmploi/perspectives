package fr.poleemploi.eventsourcing.eventstore

import fr.poleemploi.eventsourcing.{AggregateId, Event, EventPublisher, AppendedEvent}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, MustMatchers, WordSpec}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class EventStoreSpec extends WordSpec with MustMatchers
  with MockitoSugar with BeforeAndAfter {

  var eventPublisher: EventPublisher = _
  var appendOnlyStore: AppendOnlyStore = _
  var eventStore: EventStore = _
  var aggregateId: AggregateId = _

  before {
    eventPublisher = mock[EventPublisher]
    appendOnlyStore = mock[AppendOnlyStore]
    aggregateId = mock[AggregateId]
    when(aggregateId.value) thenReturn "4"

    eventStore = new EventStore(
      eventPublisher = eventPublisher,
      appendOnlyStore = appendOnlyStore
    )
  }

  "loadEventStream" should {
    "retourner une version à 0 lorsqu'au evenement n'existe" in {
      // Given
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(List())

      // When
      val eventStream = Await.result(eventStore.loadEventStream(aggregateId), 5.seconds)

      // Then
      eventStream.version mustBe 0
    }
    "retourner la version de l'evenement lorsqu'il existe" in {
      // Given
      val datas = mockAppendOnlyDatas(1)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val eventStream = Await.result(eventStore.loadEventStream(aggregateId), 5.seconds)

      // Then
      eventStream.version mustBe 1
    }
    "retourner la version du dernier evenement lorsque des evenements existent" in {
      // Given
      val datas = mockAppendOnlyDatas(7)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val eventStream = Await.result(eventStore.loadEventStream(aggregateId), 5.seconds)

      // Then
      eventStream.version mustBe 7
    }
    "retourner les evenements" in {
      // Given
      val datas = mockAppendOnlyDatas(4)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val eventStream = Await.result(eventStore.loadEventStream(aggregateId), 5.seconds)

      // Then
      eventStream.events.size mustBe datas.size
    }
    "retourner un stream d'evenements ordonnés" in {
      // Given
      val datas = mockAppendOnlyDatas(5)
      when(appendOnlyStore.readRecords(aggregateId.value)) thenReturn Future.successful(datas)

      // When
      val eventStream = Await.result(eventStore.loadEventStream(aggregateId), 5.seconds)

      // Then
      eventStream.events must contain theSameElementsInOrderAs datas.map(_.event)
    }
  }

  "append" should {
    "ne pas publier un evenement s'il n'a pas pu etre sauvegarde dans le store" in {
      // Given
      when(appendOnlyStore.append(ArgumentMatchers.eq(aggregateId.value), ArgumentMatchers.eq(0), any[List[AppendOnlyData]])) thenReturn Future.failed(new RuntimeException("Erreur de sauvegarde"))

      // When
      Await.ready(eventStore.append(
        aggregateId = aggregateId,
        expectedVersion = 0,
        events = List(mock[Event])
      ), 5.seconds)

      // Then
      verify(eventPublisher, never()).publish(any[AppendedEvent])
    }
    "sauvegarder un evenement dans le store meme si la publication echoue" in {
      // Given
      when(appendOnlyStore.append(ArgumentMatchers.eq(aggregateId.value), ArgumentMatchers.eq(0), ArgumentMatchers.any[List[AppendOnlyData]])) thenReturn Future.successful()
      when(eventPublisher.publish(ArgumentMatchers.any[AppendedEvent]())) thenReturn Future.failed(new RuntimeException("Erreur de publication"))

      // When & Then
      Await.ready(eventStore.append(
        aggregateId = aggregateId,
        expectedVersion = 0,
        events = List(mock[Event])
      ), 5.seconds)
    }
  }

  private def mockAppendOnlyDatas(size: Int): List[AppendOnlyData] = {
    List.tabulate[AppendOnlyData](size)(
     n => {
        val data = mock[AppendOnlyData]
        when(data.streamVersion) thenReturn n + 1
        when(data.event) thenReturn mock[Event]
        data
      }
    )
  }
}
