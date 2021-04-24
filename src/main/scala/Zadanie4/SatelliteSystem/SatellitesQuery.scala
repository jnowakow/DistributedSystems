package Zadanie4.SatelliteSystem

import Zadanie4.DB.DBWrite
import Zadanie4.SatelliteApi.{OK, Status}
import Zadanie4.Station.Station
import Zadanie4.dbDispatcherSelector
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, TimerScheduler}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}

import scala.concurrent.duration.FiniteDuration

class SatellitesQuery(context: ActorContext[SatellitesQuery.Command],
                      timers: TimerScheduler[SatellitesQuery.Command],
                      satellitesMap: Map[Int, ActorRef[Satellite.Command]],
                      requestId: Int,
                      replyTo: ActorRef[Station.RespondStates],
                      timeOut: FiniteDuration,
                     )
                      extends AbstractBehavior[SatellitesQuery.Command](context) {

  import SatellitesQuery._

  timers.startSingleTimer(CollectionTimeout, CollectionTimeout, timeOut)

  private val respondAdapter = context.messageAdapter(WrappedRespondStatus.apply)

  private var replies = Map.empty[Int, Status]
  private var stillWaiting = satellitesMap.keySet

  satellitesMap foreach {
    case (_, ref) =>
      ref ! Satellite.GetStatus(respondAdapter)
  }


  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case WrappedRespondStatus(respondStatus) => onRespondStatus(respondStatus)
      case CollectionTimeout => onCollectionTimeout()
    }
  }

  private def onRespondStatus(respondStatus: Satellite.RespondStatus): Behavior[Command] = {
    val status = respondStatus.status
    val id = respondStatus.id

    replies += (id -> status)
    stillWaiting -= id

    if (stillWaiting.isEmpty) {
      val percentage = 1.0
      val problems = filterProblems()

      replyTo ! Station.RespondStates(requestId, problems, percentage)
      context.spawnAnonymous[Nothing](DBWrite(problems.keySet), DispatcherSelector.fromConfig("my-blocking-dispatcher"))
      Behaviors.stopped
    }
    else {
      this
    }


  }

  private def onCollectionTimeout(): Behavior[Command] = {
    val percentage = replies.size.toDouble / (replies.size + stillWaiting.size)
    val problems = filterProblems()

    replyTo ! Station.RespondStates(requestId, problems, percentage)
    context.spawnAnonymous[Nothing](DBWrite(problems.keySet), dbDispatcherSelector)

    Behaviors.stopped
  }

  private def filterProblems(): Map[Int, Status] = replies filter { case (_, status) => status != OK}
}

object SatellitesQuery {
  def apply(
           satellitesMap: Map[Int, ActorRef[Satellite.Command]],
           requestId: Int,
           replyTo: ActorRef[Station.RespondStates],
           timeOut: FiniteDuration,
           ): Behavior[Command] =
    Behaviors.setup(context =>
      Behaviors.withTimers(timers =>
        new SatellitesQuery(context, timers, satellitesMap, requestId, replyTo, timeOut)
      )
    )

  trait Command
  private case object CollectionTimeout extends Command
  final case class WrappedRespondStatus(respondStatus: Satellite.RespondStatus) extends Command
}

