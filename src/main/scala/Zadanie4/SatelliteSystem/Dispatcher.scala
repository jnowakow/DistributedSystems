package Zadanie4.SatelliteSystem

import Zadanie4.DB.DBQuery
import Zadanie4.{dbDispatcherSelector, dispatcherSelector}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import Zadanie4.Station.Station
import scala.concurrent.duration._

class Dispatcher(context: ActorContext[Dispatcher.Command]) extends AbstractBehavior[Dispatcher.Command](context) {
  import Dispatcher._

  private var satellites = Map[Int, ActorRef[Satellite.Command]]()



  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RegisterSatellite(id) =>
        val newSatellite = context.spawn(Satellite(id), s"Satellite-$id", dispatcherSelector)
        satellites += id -> newSatellite
        this
      case SatellitesStatus(queryId, firstId, range, timeout, replyTo) =>
        val idsSet = (firstId until (firstId + range)).toSet
        val queriedSatellites = satellites filter { case (id, _) => idsSet contains id }

        context.spawnAnonymous(
          SatellitesQuery(queriedSatellites, queryId, replyTo, timeout.milliseconds),
          dispatcherSelector
        )
        this
      case SatelliteErrors(satelliteId, replyTo) =>

        context.spawnAnonymous[Nothing](
          DBQuery(satelliteId, replyTo),
          dbDispatcherSelector
        )

        this
    }
  }

}

object Dispatcher {

  def apply(): Behavior[Command] = Behaviors.setup(context => new Dispatcher(context))


  sealed trait Command
  final case class SatellitesStatus(queryId: Int, firstId: Int, range: Int, timeout: Int, replyTo: ActorRef[Station.RespondStates])
    extends Command
  final case class SatelliteErrors(satelliteId: Int, replyTo: ActorRef[Station.RespondSatelliteErrors]) extends Command
  final case class RegisterSatellite(id: Int) extends Command

}