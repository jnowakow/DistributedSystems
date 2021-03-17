import com.rabbitmq.client.{BuiltinExchangeType, Channel}

package object Zadanie2 {

  def declareExchange(channel: Channel, name: String, exchangeType: BuiltinExchangeType, durable: Boolean = false): Unit = {
    channel.exchangeDeclare(name, exchangeType, durable)
  }

  def declareQueue(channel: Channel, name: String, durable: Boolean = false, exclusive: Boolean = false, autoDelete: Boolean = false): Unit = {
    channel.queueDeclare(name, durable, exclusive, autoDelete, null)
  }
}
