package Zadanie1.Server

import java.io.{BufferedReader, PrintStream}
import java.net.{DatagramPacket, InetAddress, Socket}

case class TcpConnection(socket: Socket, in: BufferedReader, out: PrintStream)

case class UdpConnection(address: InetAddress, port: Int)

case class User(name: String, tcpConnection: TcpConnection, udpConnection: UdpConnection) {

  private val in = tcpConnection.in

  private val out = tcpConnection.out

  def nonBlockingRead: Option[String] = {
    if (in.ready)
      Some(in.readLine)
    else
      None
  }

  def blockingRead: String = in.readLine()

  def tcpWrite(msg: String): Unit = out.println(msg)

  def closeTcp(): Unit = tcpConnection.socket.close()

  def createPacket(msg: String): DatagramPacket = {
    val bytes = msg.getBytes
    new DatagramPacket(bytes, bytes.length, udpConnection.address, udpConnection.port)
  }
}
