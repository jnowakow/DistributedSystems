package Zadanie1.Server

import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala

class SimpleUserMap {
  var users = new ConcurrentHashMap[String, User]().asScala


  def register(user: User): Unit = users += (user.name -> user)

  def unregister(user: User): Unit = users -= user.name

  def checkUserName(userName: String): Boolean = users.keySet contains userName

}
