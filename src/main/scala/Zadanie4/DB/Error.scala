package Zadanie4.DB

import slick.jdbc.SQLiteProfile.api._

class Error(tag: Tag) extends Table[(Int, Int)](tag, "errors"){
  def id = column[Int]("id")
  def count = column[Int]("count")
  def * = (id, count)
}
