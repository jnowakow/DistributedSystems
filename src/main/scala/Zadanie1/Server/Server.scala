package Zadanie1.Server

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.{DatagramPacket, DatagramSocket, InetAddress, ServerSocket}

import sun.misc.Signal

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object Server extends App {

  val portNumber = 8000
  val serverSocket = new ServerSocket(portNumber)
  val udpSocket = new DatagramSocket(portNumber)
  val usersMap = new SimpleUserMap
  val messageList = new SimpleConcurrentMessageList

  Signal.handle(new Signal("INT"), (_: Signal) => {
    serverSocket.close()
    sys.exit(0)
  })


  println("--------Server Stared--------")


  /*** handle new connections ***/
  Future {
    while (true) {
      val socket = serverSocket.accept()
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = new PrintStream(socket.getOutputStream)
      //TODO obsługa kolizji nazw
      val name :: hostName :: port :: Nil = new String(in.readLine()).split(":").toList
      val address = InetAddress.getByName(hostName)

      val user = User(name, TcpConnection(socket, in, out), UdpConnection(address, port.toInt))

      UserThread(user, messageList, usersMap).start()
    }
  }

  /*** handle incoming udp packets from users ***/
  Future {
    while (true) {
      val buffer = Array.ofDim[Byte](1024)
      val receivePacket = new DatagramPacket(buffer, buffer.length)
      udpSocket.receive(receivePacket)

      val msg = new String(buffer)
      val name = msg.takeWhile(_ != ':')
      val content = msg.dropWhile(_ != ':').drop(1)

      for( (un, user) <- usersMap.users if name != un) {
        val packet = user.createPacket(s"$name:\n$content")
        udpSocket.send(packet)
      }
    }
  }

  /*** send messages from users overs tcp ***/
  while (true) {
    messageList.getMessage.foreach { case (userName, message) =>

      for( (un, user) <- usersMap.users if userName != un) {
        user.tcpWrite(s"$userName: $message")
      }
    }
  }

}
