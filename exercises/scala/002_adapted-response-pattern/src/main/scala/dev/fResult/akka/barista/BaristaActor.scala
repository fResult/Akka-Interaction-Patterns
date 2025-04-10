package dev.fResult.akka.barista

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import dev.fResult.akka.Coffee
import dev.fResult.akka.barista.BaristaCommand.{CoffeeReady, OrderCoffee}
import dev.fResult.akka.coffeeMachine.CoffeeMachineCommand.{BrewCoffee, PickupCoffee}
import dev.fResult.akka.coffeeMachine.{CoffeeMachineActor, CoffeeMachineCommand}

object BaristaActor:
  private final case class State(orders: Map[String, Coffee] = Map.empty)

  def apply(): Behavior[BaristaCommand] = Behaviors.setup { context =>
    val coffeeMachineActorRef = spawnCoffeeMachineActor(context)

    handleCommands(context, coffeeMachineActorRef, State())
  }

  private def handleCommands(context: ActorContext[BaristaCommand],
                             coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                             state: State,
                            ): Behavior[BaristaCommand] = Behaviors.receiveMessage {

    case cmd@OrderCoffee(whom, coffee) =>
      onOrderCoffee(cmd, context, coffeeMachineActorRef, state)

    case CoffeeReady(coffee) =>
      onCoffeeReady(context, coffeeMachineActorRef, state)
  }

  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActor: ActorRef[CoffeeMachineCommand],
                            state: State,
                           ): Behavior[BaristaCommand] = {

    val updatedState = state.copy(state.orders + (command.whom -> command.coffee))
    context.log.info(s"Barista: Orders${printOrder(updatedState.orders.toSet)}")

    coffeeMachineActor ! BrewCoffee(command.coffee, context.self)
    handleCommands(context, coffeeMachineActor, updatedState)
  }

  private def onCoffeeReady(context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                            state: State,
                           ): Behavior[BaristaCommand] = {

    coffeeMachineActorRef ! PickupCoffee
    handleCommands(context, coffeeMachineActorRef, state)
  }

  private def spawnCoffeeMachineActor(context: ActorContext[BaristaCommand]): ActorRef[CoffeeMachineCommand] =
    context.spawn(CoffeeMachineActor(), "coffee-machine")

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
end BaristaActor
