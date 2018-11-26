package fr.poleemploi.eventsourcing.infra.akka

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props, Stash, Terminated, Timers}
import fr.poleemploi.cqrs.projection.Projection
import fr.poleemploi.eventsourcing.Event
import fr.poleemploi.eventsourcing.eventstore.{AppendedEvent, EventStoreListener}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}

/**
  * Utilise un acteur principal et des routeurs pour envoyer les évenements vers toutes les projections
  * en créeant un acteur pour chaque projection
  */
class AkkaEventStoreListener(actorSystem: ActorSystem) extends EventStoreListener {

  import ProjectionManagerActor._

  val projectionManagerActor: ActorRef = actorSystem.actorOf(ProjectionManagerActor.props, "projectionManagerActor")
  val eventBus = new ProjectionEventBus(projectionManagerActor)

  override def subscribe(projections: Projection*): Unit =
    projections.foreach(p => p.listenTo.foreach(c => eventBus.subscribe(p, c)))

  override def publish(appendedEvent: AppendedEvent): Future[Unit] =
    Future.successful(eventBus.publish(appendedEvent))

  def stop(): Unit =
    projectionManagerActor ! Stop
}

object ProjectionManagerActor {

  case class Publish(projection: Projection, appendedEvent: AppendedEvent)

  case object Stop

  def props: Props = Props(new ProjectionManagerActor())
}

/**
  * Crée un acteur par Projection
  */
class ProjectionManagerActor extends Actor {

  import ProjectionManagerActor._

  val projectionActors: mutable.Map[Projection, ActorRef] = mutable.HashMap.empty

  override def receive: Receive = {
    case e: ProjectionManagerActor.Publish =>
      projectionActors.getOrElse(
        key = e.projection,
        default = {
          val actor = context.actorOf(
            props = ProjectionActor.props(projection = e.projection),
            name = s"${e.projection.getClass.getSimpleName}"
          )
          context.watch(actor)
          projectionActors += (e.projection -> actor)
          actor
        }
      ) ! ProjectionActor.Publish(e.appendedEvent)
    case Stop =>
      projectionActors.foreach(e => e._2 ! Stop)
    case Terminated(actorRef) =>
      projectionActors.find(a => a._2 == actorRef).foreach(a =>
        projectionActors.remove(a._1)
      )
      if (projectionActors.isEmpty) {
        context.stop(self)
        context.system.terminate()
      }
  }
}

object ProjectionActor {

  case class Publish(appendedEvent: AppendedEvent)

  case object Clean

  def props(projection: Projection): Props =
    Props(new ProjectionActor(
      projection = projection
    ))

  private case object CleaningKey
}

/**
  * Crée un acteur par aggrégat
  */
class ProjectionActor(projection: Projection) extends Actor with Stash with Timers with ActorLogging {

  import AggregatActor._
  import ProjectionActor._
  import ProjectionManagerActor._

  // Recrée régulièrement les acteurs par aggrégat pour éviter d'en avoir trop en mémoire
  timers.startPeriodicTimer(CleaningKey, Clean, 1.minute)

  val aggregatActors: mutable.Map[String, ActorRef] = mutable.HashMap.empty

  override def receive: Receive = {
    case e: ProjectionActor.Publish =>
      aggregatActors.getOrElse(
        key = e.appendedEvent.streamName,
        default = {
          val actor = context.actorOf(
            props = AggregatActor.props(projection = projection, streamName = e.appendedEvent.streamName),
            name = s"${e.appendedEvent.streamName}"
          )
          context.watch(actor)
          aggregatActors += (e.appendedEvent.streamName -> actor)
          actor
        }
      ) ! Apply(e.appendedEvent.event)
    case Clean =>
      aggregatActors.foreach(e => e._2 ! PoisonPill)
      context.become(cleaning)
    case Stop =>
      timers.cancel(CleaningKey)
      aggregatActors.foreach(e => e._2 ! PoisonPill)
      context.become(stopping)
  }

  def cleaning: Receive = {
    case Terminated(actorRef) =>
      aggregatActors.find(a => a._2 == actorRef).foreach(a =>
        aggregatActors.remove(a._1)
      )
      if (aggregatActors.isEmpty) {
        context.unbecome()
        unstashAll()
      }
    case Clean => ()
    case _ => stash()
  }

  def stopping: Receive = {
    case Terminated(actorRef) =>
      aggregatActors.find(a => a._2 == actorRef).foreach(a =>
        aggregatActors.remove(a._1)
      )
      if (aggregatActors.isEmpty)
        context.stop(self)
    case _ => ()
  }
}

object AggregatActor {

  case class Apply(event: Event)

  def props(projection: Projection,
            streamName: String): Props =
    Props(new AggregatActor(
      projection = projection,
      streamName = streamName
    ))
}

/**
  * Traite les événements reçus en bloquant par aggregat pour ne pas avoir de concurrence à gérer dans la projection
  */
class AggregatActor(projection: Projection, streamName: String) extends Actor with ActorLogging {

  import AggregatActor._

  override def receive: Receive = {
    case e: Apply =>
      Await.ready(projection.onEvent.apply(e.event) recoverWith {
        case t: Throwable => Future.successful(
          log.error(t, s"Erreur lors du traitement de l'evenement ${e.event} par la projection ${projection.getClass.getName} : ${t.getMessage}")
        )
      }, Duration.Inf)
  }
}