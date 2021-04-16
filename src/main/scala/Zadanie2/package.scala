import com.rabbitmq.client.{BuiltinExchangeType, Channel, DeliverCallback}

package object Zadanie2 {

  def declareExchange(channel: Channel, name: String, exchangeType: BuiltinExchangeType, durable: Boolean = false): Unit = {
    channel.exchangeDeclare(name, exchangeType, durable)
  }

  def declareQueue(channel: Channel, name: String, durable: Boolean = false, exclusive: Boolean = false, autoDelete: Boolean = false): Unit = {
    channel.queueDeclare(name, durable, exclusive, autoDelete, null)
  }

  def callbackFactory(channel: Channel, prefix: String): DeliverCallback = (_, delivery) => {
    val msg = new String(delivery.getBody, "UTF-8")
    println(s"$prefix $msg")
    channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
  }
}
