package Zadanie2.Client

import java.util.UUID

import Zadanie2.{callbackFactory, declareExchange, declareQueue}
import com.rabbitmq.client.{BuiltinExchangeType, CancelCallback, Channel, ConnectionFactory, DeliverCallback}

import scala.io.StdIn.readLine

object Client {

  val factory = new ConnectionFactory()
  factory.setHost("localhost")

  val connection = factory.newConnection()

  case class Admin(queueName: String, channel: Channel)
  case class Order(exchangeName: String, channel: Channel)
  case class Ack(queueName: String, channel: Channel)


  def setUp(name: String): (Admin, Order, Ack) = {

    /** Connection to admin **/
    val adminChannel = connection.createChannel()
    val adminExchangeName = "Management"
    val adminQueueName = adminExchangeName.toLowerCase + "." + name.toLowerCase

    declareExchange(adminChannel, adminExchangeName, BuiltinExchangeType.TOPIC)
    declareQueue(adminChannel, adminQueueName, exclusive = true, autoDelete = true)
    adminChannel.queueBind(adminQueueName, adminExchangeName, "#.client")
    val admin = Admin(adminQueueName, adminChannel)

    /** Order connection **/
    val orderChannel = connection.createChannel()
    val orderExchangeName = "Order"

    declareExchange(orderChannel, orderExchangeName, BuiltinExchangeType.TOPIC, true)

    val order = Order(orderExchangeName, orderChannel)

    /** Client acknowledgement **/
    val ackChannel = connection.createChannel()
    val ackExchangeName = "Acknowledgement"
    val ackQueueName = ackExchangeName.toLowerCase + "." + name

    declareExchange(ackChannel, ackExchangeName, BuiltinExchangeType.TOPIC, true)
    declareQueue(ackChannel, ackQueueName, exclusive = true, autoDelete = true)
    ackChannel.queueBind(ackQueueName, ackExchangeName, name)

    val ack = Ack(ackQueueName, ackChannel)
    (admin, order, ack)
  }



  def main(args: Array[String]): Unit = {
    assert(args.length == 1, "Gave name to your client")

    val name = args(0)
    val uid = UUID.randomUUID().toString
    val id = name + "#" + uid

    val (admin, order, ack) = setUp(id)
    val cancelCallback: CancelCallback = _ => {}

    val adminCallback: DeliverCallback = callbackFactory(admin.channel, "Admin:")

    admin.channel.basicConsume(admin.queueName, false, adminCallback, cancelCallback)

    val ackCallback: DeliverCallback = callbackFactory(ack.channel, "Acknowledgment: ")

    ack.channel.basicConsume(ack.queueName, false, ackCallback, cancelCallback)

    println("Now You can type next products names!")
    var input = readLine
    while (input != ":quit"){

      order.channel.basicPublish(order.exchangeName, id + "." + input, null, input.getBytes("UTF-8") )

      input = readLine
    }


    order.channel.close()
    ack.channel.close()
    admin.channel.close()
    connection.close()
  }

}