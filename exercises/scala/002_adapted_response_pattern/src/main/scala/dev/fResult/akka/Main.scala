package dev.fResult.akka

import akka.actor.typed.ActorSystem

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.control.Exception

@main
def main(): Unit = {
  val actorSystem = ActorSystem(BaristaActor(), "barista")

  actorSystem ! "Hello"

  println(">>> Press ENTER to exit <<<")
  readLine()
  Exception.ignoring(classOf[IOException])

  actorSystem.terminate()
}
