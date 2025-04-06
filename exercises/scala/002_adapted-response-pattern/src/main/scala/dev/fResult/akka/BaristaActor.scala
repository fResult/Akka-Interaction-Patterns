package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable

object BaristaActor:
  def apply(): Behavior[OrderCoffee] = Behaviors.setup(BaristaBehavior(_))

  private def printOrder(orders: List[(String, Coffee)]): String = {
    val formattedOrders = orders.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }

  final case class OrderCoffee(whom: String, coffee: Coffee)

  private class BaristaBehavior(context: ActorContext[OrderCoffee]) extends AbstractBehavior[OrderCoffee](context):
    private val orders: mutable.Map[String, Coffee] = mutable.Map()

    override def onMessage(message: OrderCoffee): Behavior[OrderCoffee] = {
      orders.put(message.whom, message.coffee)

      context.log.info(s"Orders: ${printOrder(orders.toList)}")

      Behaviors.same
    }
  end BaristaBehavior
end BaristaActor
