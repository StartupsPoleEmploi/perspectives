package fr.poleemploi.eventsourcing.infra.akka

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.eventsourcing.eventstore.AppendedEvent
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ProjectionActorSpec extends TestKit(ActorSystem("MySpec")) with ImplicitSender
  with WordSpecLike with MockitoSugar with MustMatchers with BeforeAndAfterAll {

  var projection: Projection = _
  val PUBLISHED_EVENT = "PUBLISHED_EVENT"

  override protected def beforeAll(): Unit = {
    projection = mock[Projection]
    when(projection.listenTo) thenReturn List(classOf[Event])
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "ProjectionActor" must {
    "publier le message à la projection" in {
      val projectionActor = system.actorOf(ProjectionActor.props(
        projection = projection
      ))
      val echo = system.actorOf(TestActors.echoActorProps)
      val appendedEvent = mockAppendedEvent
      when(projection.onEvent).thenReturn(onEventSuccess(echo))

      projectionActor ! ProjectionActor.Publish(appendedEvent)

      expectMsg(PUBLISHED_EVENT)
    }
    "publier le message à la projection après déclenchement d'un nettoyage (ne doit pas gêner le fonctionnement normal)" in {
      val projectionActor = system.actorOf(ProjectionActor.props(
        projection = projection,
        cleanInterval = 500.milliseconds
      ))
      val echo = system.actorOf(TestActors.echoActorProps)
      val appendedEvent = mockAppendedEvent
      when(projection.onEvent).thenReturn(onEventSuccess(echo))

      Future(Thread.sleep(1000)).foreach(_ =>
        projectionActor ! ProjectionActor.Publish(appendedEvent)
      )

      expectMsg(PUBLISHED_EVENT)
    }
    "arrêter l'acteur lorsqu'aucun aggregat n'est en cours" in {
      val projectionActor = system.actorOf(ProjectionActor.props(
        projection = projection
      ))
      val probe = TestProbe()
      probe watch projectionActor

      projectionActor ! ProjectionActor.Stop

      probe.expectTerminated(projectionActor)
    }
  }

  private def mockAppendedEvent: AppendedEvent = {
    val appendedEvent = mock[AppendedEvent]
    val event = mock[Event]
    when(appendedEvent.streamName) thenReturn UUID.randomUUID().toString
    when(appendedEvent.streamVersion) thenReturn 0
    when(appendedEvent.event) thenReturn event
    appendedEvent
  }

  private def onEventSuccess(testActor: ActorRef): PartialFunction[Event, Future[Unit]] = {
    case _: Event => Future.successful(testActor ! PUBLISHED_EVENT)
  }

}
