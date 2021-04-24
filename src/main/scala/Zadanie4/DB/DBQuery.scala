package Zadanie4.DB

import DBUtils.db
import Zadanie4.Station.Station
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import slick.jdbc.SQLiteProfile.api._

import scala.util.{Failure, Success}

object DBQuery {

  def apply(id: Int, replyTo: ActorRef[Station.RespondSatelliteErrors]): Behavior[Nothing] =
    Behaviors.setup[Nothing](context => {

      val error = TableQuery[Error]
      val read = error.filter(_.id === id).result

      db.run(read).onComplete {
        case Failure(exception) => context.log.error(exception.getMessage)
        case Success(Seq((id, errorsCount))) =>
          replyTo ! Station.RespondSatelliteErrors(id, errorsCount)
      }(context.executionContext
      )
      Behaviors.stopped[Nothing]
    })
}
