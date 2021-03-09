package Zadanie1.Client

import java.io.{BufferedReader, InputStreamReader, PrintStream}
import java.net.Socket

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
  val serverAddress = "localhost"
  val socket = new Socket(serverAddress, serverPort)

  Signal.handle(new Signal("INT"), (_: Signal) => {
    out.println(":quit")
    socket.close()
    sys.exit(0)
  })


  val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
  val out = new PrintStream(socket.getOutputStream)

  out.println(name)


  var stopped = false
  Future {
      while (!stopped) {
        val message = in.readLine()
        if (message != null) println(message)
      }
  }

  var input = ""
  while (!input.equalsIgnoreCase(":quit")){
    input = readLine
    out.println(input)
  }
  stopped = true
  socket.close()
}
