package dev.fResult.akka

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import dev.fResult.akka.BaristaCommand.OrderCoffee
import dev.fResult.akka.CoffeeMachineCommand.BrewCoffee
import dev.fResult.akka.{Coffee, CoffeeMachineActor, CoffeeMachineCommand}

import scala.collection.mutable

object BaristaActor:
  private val orders = mutable.Map[String, Coffee]()

  def apply(): Behavior[OrderCoffee] = Behaviors.setup(context => {
    val coffeeMachineActorRef = context.spawn(CoffeeMachineActor(), "coffee-machine")

    Behaviors.receiveMessage {
      case OrderCoffee(whom, coffee) => onOrderCoffee(OrderCoffee(whom, coffee), context, coffeeMachineActorRef)
    }
  })


  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[OrderCoffee],
                            coffeeMachineActor: ActorRef[CoffeeMachineCommand]
                           ): Behavior[OrderCoffee] = {

    orders.put(command.whom, command.coffee)

    context.log.info(s"Orders: ${printOrder(orders.toSet)}")
    coffeeMachineActor ! BrewCoffee(command.coffee)

    Behaviors.same
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
end BaristaActor
