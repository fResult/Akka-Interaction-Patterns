package dev.fResult.akka

import akka.actor.typed.ActorSystem
import dev.fResult.akka.barista.BaristaCommand.OrderCoffee
import dev.fResult.akka.Coffee.{Akkacino, CaffeJava}
import dev.fResult.akka.barista.BaristaActor

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.control.Exception

@main
def main(): Unit = {
  val baristaActor = ActorSystem(BaristaActor(), "barista")

  println("hello ex1")
  baristaActor ! OrderCoffee("Wick", Akkacino)
  baristaActor ! OrderCoffee("Anderson", CaffeJava)

  println(">>> Press ENTER to exit <<<")
  readLine()
  Exception.ignoring(classOf[IOException])

  baristaActor.terminate()
}
