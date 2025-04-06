package dev.fResult.akka.barista

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import dev.fResult.akka.Coffee
import dev.fResult.akka.barista.BaristaCommand.{CoffeeReady, OrderCoffee}
import dev.fResult.akka.coffeeMachine.CoffeeMachineCommand.{BrewCoffee, PickupCoffee}
import dev.fResult.akka.coffeeMachine.{CoffeeMachineActor, CoffeeMachineCommand}

import scala.collection.mutable

object BaristaActor:
  private val orders = mutable.Map[String, Coffee]()

  def apply(): Behavior[BaristaCommand] = Behaviors.setup(context => {
    val coffeeMachineActorRef = context.spawn(CoffeeMachineActor(), "coffee-machine")

    Behaviors.receiveMessage {
      case OrderCoffee(whom, coffee) => onOrderCoffee(OrderCoffee(whom, coffee), context, coffeeMachineActorRef)
      case CoffeeReady(coffee) => onCoffeeReady(coffee, context, coffeeMachineActorRef)
    }
  })


  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActor: ActorRef[CoffeeMachineCommand]
                           ): Behavior[BaristaCommand] = {

    orders.put(command.whom, command.coffee)
    context.log.info(s"Orders: ${printOrder(orders.toSet)}")

    coffeeMachineActor ! BrewCoffee(command.coffee, context.self)

    Behaviors.same
  }

  private def onCoffeeReady(coffee: Coffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand]
                           ): Behavior[BaristaCommand] = {

    coffeeMachineActorRef ! PickupCoffee(coffee)

    Behaviors.same
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
end BaristaActor
