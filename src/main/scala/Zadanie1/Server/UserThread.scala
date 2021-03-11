package Zadanie1.Server

class UserThread(user: User, messageList: SimpleConcurrentMessageList, userMap: SimpleUserMap) extends Thread {

  private var stopped = false

  override def run(): Unit = {

    userMap.register(user)
    println(s"registered ${user.name}")


    while (!stopped) {
      user.nonBlockingRead.foreach { message =>
        if(message.equalsIgnoreCase(":quit")){
          user.closeTcp
          userMap.unregister(user)
          println(s"unregistered ${user.name}")
          stopped = true
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
