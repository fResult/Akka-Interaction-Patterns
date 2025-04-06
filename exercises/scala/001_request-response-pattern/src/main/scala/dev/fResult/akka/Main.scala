package dev.fResult.akka

import akka.actor.typed.ActorSystem
import dev.fResult.akka.BaristaCommand.OrderCoffee
import dev.fResult.akka.Coffee.{Akkacino, CaffeeJava}

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.control.Exception

@main
def main(): Unit = {
  val baristaActor = ActorSystem(BaristaActor(), "barista")

  println("hello ex1")
  baristaActor ! OrderCoffee("Wick", Akkacino)
  baristaActor ! OrderCoffee("Anderson", CaffeeJava)

  println(">>> Press ENTER to exit <<<")
  readLine()
  Exception.ignoring(classOf[IOException])

  baristaActor.terminate()
}
