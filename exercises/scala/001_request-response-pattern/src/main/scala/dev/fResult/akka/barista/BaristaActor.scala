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

    handleCommands(context, State())
  })

  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActor: ActorRef[CoffeeMachineCommand],
                            state: State
                           ): Behavior[BaristaCommand] = {

    val newState = state.copy(state.orders + (command.whom -> command.coffee))
    context.log.info(s"Barista: Orders${printOrder(newState.orders.toSet)}")

    coffeeMachineActor ! BrewCoffee(command.coffee, context.self)

    handleCommands(context, newState)
  }

  private def onCoffeeReady(context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                            state: State
                           ): Behavior[BaristaCommand] = {
    coffeeMachineActorRef ! PickupCoffee

    handleCommands(context, state)
  }

  private def handleCommands(context: ActorContext[BaristaCommand],
                             state: State
                            ): Behavior[BaristaCommand] = Behaviors.receiveMessage {

    case command@OrderCoffee(whom, coffee) =>
      val coffeeMachineActorRef = childCoffeeMachineActorRef(context)

      onOrderCoffee(command, context, coffeeMachineActorRef, state)

    case CoffeeReady(coffee) =>
      val coffeeMachineActorRef = childCoffeeMachineActorRef(context)

      onCoffeeReady(context, coffeeMachineActorRef, state)
  }

  private def childCoffeeMachineActorRef(context: ActorContext[BaristaCommand]): ActorRef[CoffeeMachineCommand] =
    context.child("coffee-machine").getOrElse(context.spawn(CoffeeMachineActor(), "coffee-machine"))
        .asInstanceOf[ActorRef[CoffeeMachineCommand]]

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
end BaristaActor
