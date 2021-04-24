package Zadanie2
import Zadanie4.SatelliteApi._
import Zadanie4.SatelliteSystem.Satellite
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SatelliteTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import Satellite._
  "Satellite actor" must {

    "reply with status" in {
     val probe = createTestProbe[RespondStatus]()
     val satelliteActor = spawn(Satellite(1))

     satelliteActor ! Satellite.GetStatus(probe.ref)

      val response = probe.receiveMessage()

      println(response.status)

      response.id should be (1)
      Set(OK, BATTERY_LOW, PROPULSION_ERROR, NAVIGATION_ERROR) should contain (response.status)
    }
  }

}
