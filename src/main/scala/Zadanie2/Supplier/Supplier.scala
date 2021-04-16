package Zadanie2.Supplier


import java.util.UUID
import Zadanie2.{declareExchange, declareQueue}
import com.rabbitmq.client.{AMQP, BuiltinExchangeType, CancelCallback, Channel, ConnectionFactory, Consumer, DefaultConsumer, DeliverCallback, Envelope}


object Supplier {
  val factory = new ConnectionFactory()
  factory.setHost("localhost")
  val connection = factory.newConnection()

  case class Admin(queueName: String, channel: Channel)
  case class Products(products: List[String], channel: Channel)
  case class Ack(exchange: String, channel: Channel)

  def setUp(name: String, productsNames: List[String]): (Admin, Products, Ack) = {
    val id = name

    /** Connection to admin **/
    val toAdminChannel = connection.createChannel()
    val toAdminExchangeName = "Management"
    val toAdminQueueName = toAdminExchangeName.toLowerCase + "." + id.toLowerCase

    declareExchange(toAdminChannel, toAdminExchangeName, BuiltinExchangeType.TOPIC)
    declareQueue(toAdminChannel, toAdminQueueName, exclusive = true, autoDelete = true)
    toAdminChannel.queueBind(toAdminQueueName, toAdminExchangeName, "supplier.#")
    val admin = Admin(toAdminQueueName, toAdminChannel)

    /** Order connection **/
    val orderChannel = connection.createChannel()
    val orderExchangeName = "Order"

    orderChannel.basicQos(1)
    declareExchange(orderChannel, orderExchangeName, BuiltinExchangeType.TOPIC, true)

    productsNames.foreach { product =>
      declareQueue(orderChannel, product.capitalize, true )
      orderChannel.queueBind(product.capitalize, orderExchangeName, "*." + product.toLowerCase)
    }
    val products = Products(productsNames.map(_.capitalize), orderChannel)

    /** Client acknowledgement **/
    val ackChannel = connection.createChannel()
    val ackExchangeName = "Acknowledgement"

    declareExchange(ackChannel, ackExchangeName, BuiltinExchangeType.TOPIC, true)
    val ack = Ack(ackExchangeName, ackChannel)
    (admin, products, ack )

  }

  def main(args: Array[String]): Unit = {
    assert(args.length >= 2, "Specify name and at least one product")

    val name = args(0)
    val productsNames = args.tail.toList

    val (admin, products, ack) = setUp(name, productsNames)

    val productConsumer: Consumer = new DefaultConsumer(products.channel) {
      @Override override def handleDelivery(consumerTag: String,
                                            envelope: Envelope,
                                            properties: AMQP.BasicProperties,
                                            body: Array[Byte]): Unit = {

        val msg = new String(body, "UTF-8")
        val orderId = UUID.randomUUID().toString
        val routingKey =  envelope.getRoutingKey
        val deliveryTag = envelope.getDeliveryTag
        val customerId = routingKey.split("\\.").head
        val name = customerId.split("#").head
        println(
          s"""Received: $msg from $name
             |order id: $orderId""".stripMargin
        )

        ack.channel.basicPublish(ack.exchange, customerId,  null, s"order number $orderId".getBytes("UTF-8"))

        getChannel.basicAck(deliveryTag, false)
      }
    }

    products.products.foreach { product =>
      products.channel.basicConsume(product,false, productConsumer)
    }

    val adminCallback: DeliverCallback = (_, delivery) => {
      val msg = new String(delivery.getBody, "UTF-8")
      println(s"Admin: $msg")
      admin.channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
    }

    val cancelCallback: CancelCallback = _ => {}

    admin.channel.basicConsume(admin.queueName, false, adminCallback,cancelCallback)

  }
}

