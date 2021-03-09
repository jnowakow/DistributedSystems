package Zadanie1.Server

import java.io.{BufferedReader, PrintStream}
import java.net.Socket

case class User(name: String, socket: Socket, in: BufferedReader, out: PrintStream)
