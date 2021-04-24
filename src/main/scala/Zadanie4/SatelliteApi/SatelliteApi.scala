package Zadanie4.SatelliteApi


import scala.util.Random


sealed trait Status

case object OK extends Status
case object BATTERY_LOW extends Status
case object PROPULSION_ERROR extends Status
case object NAVIGATION_ERROR extends Status


trait SatelliteApi {
  def getStatus(): Status = {
    val rand = new Random()

    val timeout = 100 + rand.nextInt(400)
    Thread.sleep(timeout)

    rand.nextDouble() match {
      case p if p < 0.8 => OK
      case p if p < 0.9 => BATTERY_LOW
      case p if p < 0.95 => NAVIGATION_ERROR
      case _ => PROPULSION_ERROR
    }
  }
}
