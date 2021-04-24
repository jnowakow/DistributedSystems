package Zadanie4.DB

import Zadanie4.dbUrl
import slick.jdbc.SQLiteProfile.api._


object DBUtils {
  lazy val db = Database.forURL(dbUrl, driver = "org.sqlite.JDBC")


  def initializeDB(ids: Set[Int]): Unit = {
    val errorTable = TableQuery[Error]

    val entries = ids.map(id => (id, 0)).toSeq

    db.run(errorTable.delete andThen (errorTable ++= entries))
  }
}
