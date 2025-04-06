package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object BaristaActor {
  def apply(): Behavior[String] = Behaviors.setup(BaristaBehavior(_))

  private class BaristaBehavior(context: ActorContext[String]) extends AbstractBehavior[String](context) {

    override def onMessage(message: String): Behavior[String] = {
      println(s"Hello, $message!")

      Behaviors.same
    }
  }
}
