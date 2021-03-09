package Zadanie1.Server

import scala.annotation.tailrec

class SimpleConcurrentMessageList {
  sealed trait MyList
  case class Node(userName: String, message: String, time: Long,next: MyList) extends MyList
  case object Empty extends MyList

  var list: MyList = Empty

  def addMessage(userName: String, message: String, time: Long): Unit = synchronized {

    def insertRec(elem: MyList): MyList = {
      elem match {
        case Empty => Node(userName, message, time, Empty)
        case n @ Node(u, m, t, next) =>
          if (time > t)
            Node(u, m, t ,insertRec(next))
          else
            Node(userName, message, time, n)
      }
    }

    list = insertRec(list)
  }

  def getMessage: Option[(String, String)] = synchronized {
    list match {
      case Empty => None
      case Node(userName, message, _, next) =>
        list = next
        Some((userName, message))
    }
  }

}
