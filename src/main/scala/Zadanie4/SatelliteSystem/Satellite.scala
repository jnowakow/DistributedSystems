package Zadanie4.SatelliteSystem

import Zadanie4.SatelliteApi.{SatelliteApi, Status}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}


class Satellite(context: ActorContext[Satellite.Command], id: Int)
  extends AbstractBehavior[Satellite.Command](context) with SatelliteApi {

  import Satellite._

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case GetStatus(replyTo) =>
        replyTo ! RespondStatus(id, getStatus())
        this
    }
  }
}

object Satellite {

  def apply(id: Int): Behavior[Command] =
    Behaviors.setup(context => new Satellite(context, id))


  sealed trait Command
  final case class GetStatus(replyTo: ActorRef[RespondStatus]) extends Command
  final case class RespondStatus(id: Int, status: Status) extends Command
}

