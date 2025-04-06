package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable

object BaristaActor {
  def apply(): Behavior[OrderCoffee] = Behaviors.setup(BaristaBehavior(_))

  final case class OrderCoffee(whom: String, coffee: Coffee)

  private final class BaristaBehavior(context: ActorContext[OrderCoffee]) extends AbstractBehavior[OrderCoffee](context):
    private val orders = mutable.Map[String, Coffee]()

    override def onMessage(command: OrderCoffee): Behavior[OrderCoffee] = {
      orders.put(command.whom, command.coffee)

      context.log.info(s"Greet, $command")

      Behaviors.same
    }
}
