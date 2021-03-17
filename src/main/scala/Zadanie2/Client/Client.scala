package Zadanie2.Client

import Zadanie2.{declareExchange, declareQueue}
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
    val ackQueueName = ackExchangeName.toLowerCase + "." + name.toLowerCase

    declareExchange(ackChannel, ackExchangeName, BuiltinExchangeType.DIRECT, true)
    declareQueue(ackChannel, ackQueueName, exclusive = true, autoDelete = true)
    ackChannel.queueBind(ackQueueName, ackExchangeName, name.toLowerCase)

    val ack = Ack(ackQueueName, ackChannel)
    (admin, order, ack)
  }

  def callbackFactory(channel: Channel, prefix: String): DeliverCallback = (_, delivery) => {
    val msg = new String(delivery.getBody, "UTF-8")
    println(s"$prefix $msg")
    channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
  }


  def main(args: Array[String]): Unit = {
    assert(args.length == 1, "Gave name to your client")

    val name = args(0)
    val (admin, order, ack) = setUp(name)
    val cancelCallback: CancelCallback = _ => {}

    val adminCallback: DeliverCallback = callbackFactory(admin.channel, "Admin:")

    admin.channel.basicConsume(admin.queueName, false, adminCallback, cancelCallback)

    val ackCallback: DeliverCallback = callbackFactory(ack.channel, "Acknowledgment: ")

    ack.channel.basicConsume(ack.queueName, false, ackCallback, cancelCallback)

    println("Now You can type next products names!")
    var input = readLine
    while (input != ":quit"){

      order.channel.basicPublish(order.exchangeName, name.toLowerCase + "." + input, null, input.getBytes("UTF-8") )

      input = readLine
    }


    order.channel.close()
    ack.channel.close()
    admin.channel.close()
    connection.close()
  }

}