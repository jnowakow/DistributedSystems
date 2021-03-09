package Zadanie1.Server

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.{ServerSocket, Socket}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object Server extends App {

  val portNumber = 8000
  val serverSocket = new ServerSocket(portNumber)
  val usersMap = new SimpleUserMap
  val messageList = new SimpleConcurrentMessageList

  println("--------Server Stared--------")

  Future {
    while (true) {
      val socket = serverSocket.accept()
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = new PrintStream(socket.getOutputStream)
      //TODO obsługa kolizji nazw
      val name = new String(in.readLine())
      UserThread(User(name, socket, in, out), messageList, usersMap).start()
    }
  }

  while (true) {
    messageList.getMessage.foreach { case (userName, message) =>

      for( (un, user) <- usersMap.users if userName != un) {
        println(un)
        user.out.println(s"$userName: $message")
      }
    }
  }

}
