package dev.fResult.akka.barista

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import dev.fResult.akka.Coffee
import dev.fResult.akka.barista.BaristaCommand.{CoffeeReady, OrderCoffee}
import dev.fResult.akka.coffeeMachine.CoffeeMachineCommand.{BrewCoffee, PickupCoffee}
import dev.fResult.akka.coffeeMachine.{CoffeeMachineActor, CoffeeMachineCommand}

object BaristaActor:
  private case class State(orders: Map[String, Coffee] = Map.empty)

  def apply(): Behavior[BaristaCommand] = Behaviors.setup(context => {
    val coffeeMachineActorRef = context.spawn(CoffeeMachineActor(), "coffee-machine")

    handleCommands(context, coffeeMachineActorRef, State())
  })

  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                            state: State
                           ): Behavior[BaristaCommand] = {

    val updatedState = state.copy(state.orders + (command.whom -> command.coffee))
    context.log.info(s"Barista: Orders${printOrder(updatedState.orders.toSet)}")

    coffeeMachineActorRef ! BrewCoffee(command.coffee, context.self)

    handleCommands(context, coffeeMachineActorRef, updatedState)
  }

  private def onCoffeeReady(context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                            state: State
                           ): Behavior[BaristaCommand] = {

    coffeeMachineActorRef ! PickupCoffee

    handleCommands(context, coffeeMachineActorRef, state)
  }

  private def handleCommands(context: ActorContext[BaristaCommand],
                             coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                             state: State
                            ): Behavior[BaristaCommand] = Behaviors.receiveMessage {

    case command@OrderCoffee(whom, coffee) =>
      onOrderCoffee(command, context, coffeeMachineActorRef, state)

    case CoffeeReady(coffee) =>
      onCoffeeReady(context, coffeeMachineActorRef, state)
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
end BaristaActor
