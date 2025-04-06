package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object BaristaActor {
  def apply(): Behavior[OrderCoffee] = Behaviors.setup(BaristaBehavior(_))

  private final class BaristaBehavior(context: ActorContext[OrderCoffee]) extends AbstractBehavior[OrderCoffee](context):
    override def onMessage(command: OrderCoffee): Behavior[OrderCoffee] = {
      context.log.info(s"Greet, $command")

      Behaviors.same
    }

  final case class OrderCoffee(whom: String, coffee: Coffee)
}
