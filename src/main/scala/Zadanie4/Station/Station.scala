package Zadanie4.Station

import Zadanie4.SatelliteApi.Status
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import Zadanie4.SatelliteSystem.Dispatcher

class Station(context: ActorContext[Station.Command], name: String, dispatcher: ActorRef[Dispatcher.Command])
  extends AbstractBehavior[Station.Command](context) {

  import Station._

  private var queries = Map.empty[Int, Long]
  private var nextQueryId = 1

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case SendRequest(first, range, timeout) =>
        queries += nextQueryId -> System.currentTimeMillis()
        dispatcher ! Dispatcher.SatellitesStatus(nextQueryId, first, range, timeout, context.self)
        nextQueryId += 1
        this
      case RespondSatelliteErrors(satelliteId, errorsCount) =>
        if (errorsCount > 0) {
          val text =
            s"""
               |=====================
               |$name
               |$satelliteId had $errorsCount errors
               |=====================
               |""".stripMargin
          println(text)
        }
        this
      case SendDbQuery(satelliteId) =>
        dispatcher ! Dispatcher.SatelliteErrors(satelliteId, context.self)
        this
      case RespondStates(queryId, states, responsePercentage) if queries.keySet contains queryId =>
        val start = queries(queryId)
        val end = System.currentTimeMillis()

        val problemString = states.map{ case (i, status) => s"$i: $status"}.mkString("\n")

        val text =s"""
            |=====================
            |$name
            |duration: ${end - start}
            |problems: ${states.size}
            |response percentage: ${responsePercentage}
            |problems list:
            |$problemString
            |=====================
            |""".stripMargin

        println(text)
        queries -= queryId
        this
    }
  }
}


object Station {

  def apply(name: String, dispatcher: ActorRef[Dispatcher.Command]): Behavior[Command] =
    Behaviors.setup(context => new Station(context, name, dispatcher))

  trait Command
  final case class RespondStates(queryId: Int, states: Map[Int, Status], responsePercentage: Double) extends Command
  final case class SendRequest(first: Int, range: Int, timeout: Int) extends Command
  final case class SendDbQuery(satelliteId: Int) extends Command
  final case class RespondSatelliteErrors(satelliteId: Int, errorsCount: Int) extends Command

}
