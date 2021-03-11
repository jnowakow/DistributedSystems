package Zadanie1.Client

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.{DatagramPacket, DatagramSocket, InetAddress, MulticastSocket, Socket}

import sun.misc.Signal

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine

object Client extends App {
  val name = {
    var n = ""
    while (n == "") {
      print("Enter Name: ")
      n = readLine()
    }
    n.replace("\n", "")
  }

  val serverPort = 8000
  val multicastPort = 8001
  val serverAddress = "localhost"
  val inetAddress = InetAddress.getByName("localhost")

  val udpSocket = new DatagramSocket()
  val tcpSocket = new Socket(serverAddress, serverPort)
  val multicastSocket = new MulticastSocket(multicastPort)
  val groupAddress = InetAddress.getByName("230.0.0.0")
  multicastSocket.joinGroup(groupAddress)

  Signal.handle(new Signal("INT"), (_: Signal) => {
    out.println(":quit")
    tcpSocket.close()
    udpSocket.close()
    multicastSocket.leaveGroup(groupAddress)
    multicastSocket.close()
    sys.exit(0)
  })


  val in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream))
  val out = new PrintStream(tcpSocket.getOutputStream)

  out.println(name + ":" + inetAddress.getHostName + ":" + udpSocket.getLocalPort)

  var stopped = false

  //handle the tcp connection
  Future {
      while (!stopped) {
        val message = in.readLine()
        if (message != null) println(message)
      }
  }

  //handle the udp connection
  Future {
    val receiveBuffer = Array.ofDim[Byte](1024)
    while (!stopped) {
      val receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length)
      udpSocket.receive(receivePacket)

      val msg = new String(receivePacket.getData)
      println(msg)
    }
  }

  //handle the multicast connection
  Future {
    val receiveBuffer = Array.ofDim[Byte](1024)
    while (!stopped) {
      val receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length)
      multicastSocket.receive(receivePacket)

      val msg = new String(receivePacket.getData)
      println(msg)
    }
  }

  //handle the user input
  var input = ""

  def sendByUdp(address: InetAddress, port: Int, additional: String = ""): Unit = {
    var udpInput = ""
    while (!input.equalsIgnoreCase(":stop")){
      input = readLine
      udpInput += (input + "\n")
    }
    val msg = name + ":" + udpInput.replace(":stop", "") + additional
    val bytes = msg.getBytes
    val sendPacket = new DatagramPacket(bytes, bytes.length, address, port)
    udpSocket.send(sendPacket)
  }

  while (!input.equalsIgnoreCase(":quit")){
    input = readLine

    input.trim match {
      case ":U" =>
       sendByUdp(inetAddress, serverPort)
      case ":M" =>
        sendByUdp(groupAddress, multicastPort, "\nmulticast socket")
      case _ =>
        out.println(input)
    }
  }

  stopped = true
  tcpSocket.close()
  udpSocket.close()
  multicastSocket.leaveGroup(groupAddress)
  multicastSocket.close()
}
