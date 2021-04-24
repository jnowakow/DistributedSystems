package Zadanie4.DB

import DBUtils.db
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import slick.jdbc.SQLiteProfile.api._


object DBWrite {

  def apply(ids: Set[Int]): Behavior[Nothing] = Behaviors.setup[Nothing](context => {

    val error = TableQuery[Error]
    val read = error.filter(_.id inSet ids).result

    db.run(read).foreach { res =>
      val updates = res.map { case (id, errorCount) =>
        error.filter(_.id === id).update((id, errorCount + 1))
      }
      db.run(DBIO.seq(updates: _*))
    }(context.executionContext)

    Behaviors.stopped[Nothing]
  })

}
