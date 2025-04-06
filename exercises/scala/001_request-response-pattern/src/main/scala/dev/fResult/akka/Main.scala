package dev.fResult.akka

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}

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

object BaristaActor {
  def apply(): Behavior[Any] = Behaviors.setup(BaristaBehavior(_))

  private final class BaristaBehavior(context: ActorContext[Any]) extends AbstractBehavior[Any](context):
    override def onMessage(msg: Any): Behavior[Any] = {
      println(s"Greet, $msg")

      Behaviors.same
    }
}
