package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import dev.fResult.akka.BaristaCommand.OrderCoffee

import scala.collection.mutable

object BaristaActor {
  private val orders = mutable.Map[String, Coffee]()

  def apply(): Behavior[OrderCoffee] = Behaviors.setup(context => {
    Behaviors.receiveMessage {
      case OrderCoffee(whom, coffee) => onOrderCoffee(OrderCoffee(whom, coffee), context)
    }
  })


  private def onOrderCoffee(command: OrderCoffee, context: ActorContext[OrderCoffee]): Behavior[OrderCoffee] = {
    orders.put(command.whom, command.coffee)

    context.log.info(s"Orders: ${printOrder(orders.toSet)}")

    Behaviors.same
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
}
