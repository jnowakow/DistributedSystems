package Zadanie1.Server

import org.scalatest.funsuite.AnyFunSuite

class SimpleConcurrentMessageListTest extends AnyFunSuite {

  test("Add elements to list") {
    val list = new SimpleConcurrentMessageList
    list.addMessage("usr1", "msg1", 1)
    list.addMessage("usr2", "msg2", 2)

    assert(list.getMessage === Some(("usr1", "msg1")))
    assert(list.getMessage === Some(("usr2", "msg2")))
    assert(list.getMessage === None)

  }

}
