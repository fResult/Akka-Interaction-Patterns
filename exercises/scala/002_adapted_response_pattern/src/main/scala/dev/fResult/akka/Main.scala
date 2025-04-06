package dev.fResult.akka

import akka.actor.typed.ActorSystem
import dev.fResult.akka.BaristaActor.OrderCoffee
import dev.fResult.akka.Coffee.{Akkacino, CaffeJava, MochaPlay}

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.control.Exception

@main
def main(): Unit = {
  val actorSystem = ActorSystem(BaristaActor(), "barista")

  actorSystem ! OrderCoffee("Wick", Akkacino)
  actorSystem ! OrderCoffee("Anderson", CaffeJava)
  actorSystem ! OrderCoffee("The One", Akkacino)
  actorSystem ! OrderCoffee("Utah", MochaPlay)

  println(">>> Press ENTER to exit <<<")
  readLine()
  Exception.ignoring(classOf[IOException])

  actorSystem.terminate()
}
