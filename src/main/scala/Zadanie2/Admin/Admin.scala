package Zadanie2.Admin

import Zadanie2.declareExchange
import com.rabbitmq.client.{BuiltinExchangeType, ConnectionFactory}

import scala.io.StdIn.readLine

object Admin extends App {
  val factory = new ConnectionFactory()
  factory.setHost("localhost")
  val connection = factory.newConnection()


  val adminChannel = connection.createChannel()
  val adminExchangeName = "Management"
  declareExchange(adminChannel, adminExchangeName, BuiltinExchangeType.TOPIC)

  def sendMsg(routingKey: String): Unit = {
    val msg = readLine("Enter message: ")

    adminChannel.basicPublish(adminExchangeName, routingKey, null, msg.getBytes("UTF-8"))
  }

  println("Available options: C - message to clients, S - message to suppliers, B - message to both")
  var input = readLine("Enter receiver: ")
  while (input != ":quit") {
    input match {
      case "C" =>
        sendMsg("client")
      case "B" =>
        sendMsg("supplier.client")
      case "S" =>
        sendMsg("supplier")
      case _ =>
        println("Available options: C - message to clients, S - message to suppliers, B - message to both")
    }

    input = readLine("Enter receiver: ")
  }

  adminChannel.close()
  connection.close()

}
