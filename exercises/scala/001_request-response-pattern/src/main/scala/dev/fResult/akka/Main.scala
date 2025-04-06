package dev.fResult.akka

import akka.actor.typed.ActorSystem

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.control.Exception

@main
def main(): Unit = {
  println("Hello Scala Exercise 1")
  val baristaActor = ActorSystem(BaristaActor(), "barista")

  baristaActor ! "Akka"

  println(">>> Press ENTER to exit <<<")
  readLine()
  Exception.ignoring(classOf[IOException])

  baristaActor.terminate()
}
