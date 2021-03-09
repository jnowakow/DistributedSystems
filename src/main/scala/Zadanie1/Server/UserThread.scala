package Zadanie1.Server

class UserThread(user: User, messageList: SimpleConcurrentMessageList, userMap: SimpleUserMap) extends Thread {

  def nonBlockingRead(): Option[String] = {
    if(user.in.ready())
      Some(user.in.readLine())
    else
      None
  }

  override def run(): Unit = {

    userMap.register(user)
    println(s"registered ${user.name}")

    while (true) {
      nonBlockingRead().foreach { message =>
        if(message.equalsIgnoreCase(":quit")){
          user.socket.close()
          userMap.unregister(user)
          println(s"unregistered ${user.name}")

        }
        else
          messageList.addMessage(user.name, message, System.currentTimeMillis())
      }
    }
  }
}

object UserThread{
  def apply(user: User, messageList: SimpleConcurrentMessageList, userMap: SimpleUserMap): UserThread = new UserThread(user, messageList, userMap)
}
