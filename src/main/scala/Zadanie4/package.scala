import akka.actor.typed.DispatcherSelector

package object Zadanie4 {

  val dispatcherSelector: DispatcherSelector = DispatcherSelector.fromConfig("my-dispatcher")
  val dbDispatcherSelector: DispatcherSelector = DispatcherSelector.fromConfig("my-blocking-dispatcher")
  val dbUrl: String = "jdbc:sqlite:/home/jan/Studia/Semestr6/Rozprochy/Zadania/src/main/scala/Zadanie4/database.db"
}
