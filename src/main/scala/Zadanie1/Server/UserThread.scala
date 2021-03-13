package Zadanie1.Server

class UserThread(var user: User, messageList: SimpleConcurrentMessageList, userMap: SimpleUserMap) extends Thread {

  private var stopped = false

  def registerUser() = {
    while (userMap.checkUserName(user.name)) {
      user.tcpWrite("Name taken")

      val newName = user.blockingRead
      user = user.copy(name = newName)
    }
    user.tcpWrite("ok")
    userMap.register(user)
    println(s"registered ${user.name}")

  }

  override def run(): Unit = {

    registerUser()

    while (!stopped) {
      user.nonBlockingRead.foreach { message =>
        if (message.equalsIgnoreCase(":quit")) {
          user.closeTcp()
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
