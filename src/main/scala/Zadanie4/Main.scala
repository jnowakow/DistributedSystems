package Zadanie4

import java.io.File

import Zadanie4.DB.DBUtils
import Zadanie4.SatelliteSystem.Dispatcher
import Zadanie4.Station.Station
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

import scala.util.Random


object Main extends App {

  val range = 50

  val timeout = 300

  private def randId(): Int = 100 + Random.nextInt(50)

  def sendRequest(station: ActorRef[Station.SendRequest]): Unit =
    station ! Station.SendRequest(first = randId(), range = range, timeout = timeout)

  def create(): Behavior[Nothing] = Behaviors.setup[Nothing](context => {
    val dispatcher = context.spawn(Dispatcher(), "dispatcher")
    val satellitesIds = (100 until 200).toSet

    DBUtils.initializeDB(satellitesIds)

    //create satellites
    for (id <- satellitesIds ) {
      dispatcher ! Dispatcher.RegisterSatellite(id)
    }

    //create stations
    val stationName = "Station"
    val station1 = context.spawn(Station(s"$stationName-1", dispatcher),s"$stationName-1")
    val station2 = context.spawn(Station(s"$stationName-2", dispatcher),s"$stationName-2")
    val station3 = context.spawn(Station(s"$stationName-3", dispatcher),s"$stationName-3")


    sendRequest(station1)
    sendRequest(station1)
    sendRequest(station2)
    sendRequest(station2)
    sendRequest(station3)
    sendRequest(station3)

    Thread.sleep(1000)

    for (id <- satellitesIds) {
      station1 ! Station.SendDbQuery(id)
    }

    Behaviors.unhandled
  })

  val configFile = new File("src/main/scala/Zadanie4/dispatcher.conf")
  val config = ConfigFactory.parseFile(configFile)

  println(config)
  val system = ActorSystem[Nothing](create(), "Satellite-System", config)

}
