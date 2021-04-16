package Zadanie2.Admin

import Zadanie2.{declareExchange, declareQueue}
import com.rabbitmq.client.{BuiltinExchangeType, CancelCallback, ConnectionFactory, DeliverCallback}

import scala.io.StdIn.readLine

object Admin extends App {
  val factory = new ConnectionFactory()
  factory.setHost("localhost")
  val connection = factory.newConnection()


  val adminChannel = connection.createChannel()
  val adminExchangeName = "Management"
  declareExchange(adminChannel, adminExchangeName, BuiltinExchangeType.TOPIC)

  val orderChannel = connection.createChannel()
  val orderExchangeName = "Order"
  val orderQueueName = "admin.order.queue"

  orderChannel.basicQos(1)
  declareExchange(orderChannel, orderExchangeName, BuiltinExchangeType.TOPIC, true)

  declareQueue(orderChannel, orderQueueName, exclusive = true)
  orderChannel.queueBind(orderQueueName, orderExchangeName, "#")

  val orderCallback: DeliverCallback = (_, delivery) => {
    val customer = delivery.getEnvelope.getRoutingKey.split("#").head
    val msg = new String(delivery.getBody, "UTF-8")
    println(s"$customer: $msg")
    orderChannel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
  }

  val cancelCallback: CancelCallback = _ => {}

  orderChannel.basicConsume(orderQueueName, false, orderCallback, cancelCallback)


  val ackChannel = connection.createChannel()
  val ackExchangeName = "Acknowledgement"
  val ackQueueName = "admin.ack.queue"

  val ackCallback: DeliverCallback = (_, delivery) => {
    val customer = delivery.getEnvelope.getRoutingKey.split("#").head
    val msg = new String(delivery.getBody, "UTF-8")
    println(s"$customer: $msg")
    ackChannel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
  }

  declareExchange(ackChannel, ackExchangeName, BuiltinExchangeType.TOPIC, true)
  declareQueue(ackChannel, ackQueueName, exclusive = true)

  ackChannel.queueBind(ackQueueName, ackExchangeName, "#")

  ackChannel.basicConsume(ackQueueName, false, ackCallback, cancelCallback)


  def sendMsg(routingKey: String): Unit = {
    val msg = readLine()

    adminChannel.basicPublish(adminExchangeName, routingKey, null, msg.getBytes("UTF-8"))
  }

  println("Available options: C - message to clients, S - message to suppliers, B - message to both")
  var input = readLine()
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

    input = readLine()
  }

  adminChannel.close()
  connection.close()

}
