package dev.fResult.akka

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import dev.fResult.akka.Coffee.{Akkacino, CaffeJava, MochaPlay}
import dev.fResult.akka.barista.BaristaCommand
import dev.fResult.akka.barista.BaristaCommand.{CoffeeReady, OrderCoffee, WrappedCoffeeMachineCoffeeReady}
import dev.fResult.akka.coffeeCommand.CoffeeMachineCommand
import dev.fResult.akka.coffeeCommand.CoffeeMachineCommand.{BrewCoffee, CoffeeReadyTick, PickupCoffee}

import java.io.IOException
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration
import scala.io.StdIn.readLine
import scala.util.control.Exception

@main
def main(): Unit = {
  val baristaActor = ActorSystem[BaristaCommand](BaristaActor(), "barista")

  baristaActor ! OrderCoffee("Wick", Akkacino)
  baristaActor ! OrderCoffee("Anderson", CaffeJava)
  baristaActor ! OrderCoffee("Constantine", MochaPlay)

  println(">>> Press ENTER to exit <<<")
  readLine()
  Exception.ignoring(classOf[IOException])

  baristaActor.terminate()
}


object BaristaActor {
  private final case class State(orders: Map[String, Coffee] = Map.empty)

  def apply(): Behavior[BaristaCommand] = Behaviors.setup(context => {
    context.log.info("Barista actor is applied")

    val initialState = State()

    val coffeeMachineActorRef = context.spawn(CoffeeMachineActor(), "coffee-machine")

    val coffeeMachineMessageAdapter = context.messageAdapter[CoffeeReady](
      response => WrappedCoffeeMachineCoffeeReady(response)
    )

    handleCommands(context, coffeeMachineActorRef, coffeeMachineMessageAdapter, initialState)
  })

  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                            adapter: ActorRef[CoffeeReady],
                            state: State): Behavior[BaristaCommand] = {

    val updatedState = state.copy(state.orders + (command.whom -> command.coffee))

    context.log.info(s"Orders: ${printOrder(updatedState.orders.toSet)}")

    coffeeMachineActorRef ! BrewCoffee(command.coffee, context.self)

    handleCommands(context, coffeeMachineActorRef, adapter, updatedState)
  }

  private def onCoffeeReady(coffee: Coffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                            adapter: ActorRef[CoffeeReady],
                            state: State,
                           ): Behavior[BaristaCommand] = {

    context.log.info(s"Barista: I'm going to pickup $coffee...")
    coffeeMachineActorRef ! PickupCoffee(coffee)

    handleCommands(context, coffeeMachineActorRef, adapter, state)
  }

  private def handleCommands(context: ActorContext[BaristaCommand],
                             coffeeMachineActorRef: ActorRef[CoffeeMachineCommand],
                             adapter: ActorRef[CoffeeReady],
                             state: State,
                            ): Behavior[BaristaCommand] = {

    Behaviors.receiveMessage {
      case cmd@OrderCoffee(_, _) => onOrderCoffee(cmd, context, coffeeMachineActorRef, adapter, state)
      case CoffeeReady(coffee) => onCoffeeReady(coffee, context, coffeeMachineActorRef, adapter, state)
      case WrappedCoffeeMachineCoffeeReady(response) => onCoffeeReady(response.coffee, context, coffeeMachineActorRef, adapter, state)
    }
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
}

object CoffeeMachineActor:
  def apply(): Behavior[CoffeeMachineCommand] = idle()

  private def idle(): Behavior[CoffeeMachineCommand] = Behaviors.setup { context =>
    context.log.info("CoffeeMachine: IDLE")

    Behaviors.receiveMessage {
      case cmd@BrewCoffee(_, _) => startBrewing(cmd, context)
      case PickupCoffee(_) => Behaviors.same
      case cmd@_ => throw new IllegalStateException("Unexpected value: " + cmd)
    }
  }

  private def startBrewing(command: BrewCoffee,
                           context: ActorContext[CoffeeMachineCommand],
                          ): Behavior[CoffeeMachineCommand] = {

    Behaviors.withTimers(timers => {
      timers.startSingleTimer(CoffeeReadyTick, FiniteDuration(3, TimeUnit.SECONDS))

      brewCoffee(command, context)
    })
  }

  private def brewCoffee(command: BrewCoffee,
                         context: ActorContext[CoffeeMachineCommand],
                        ): Behavior[CoffeeMachineCommand] = {

    context.log.info(s"CoffeeMachine: {} is brewing", command.coffee)

    Behaviors.receiveMessage {
      case CoffeeReadyTick => {
        command.replyTo ! CoffeeReady(command.coffee)

        coffeeReady(command.coffee, context)
      }
      case _ => Behaviors.same
    }
  }

  private def onPickupCoffee(coffee: Coffee,
                             context: ActorContext[CoffeeMachineCommand],
                            ): Behavior[CoffeeMachineCommand] = {

    context.log.info(s"CoffeeMachine: Picking up coffee: $coffee")
    idle()
  }

  private def coffeeReady(coffee: Coffee,
                          context: ActorContext[CoffeeMachineCommand],
                         ): Behavior[CoffeeMachineCommand] = {

    context.log.info("CoffeeMachine: Coffee {} is ready", coffee)

    Behaviors.receiveMessage {
      case cmd@BrewCoffee(_, _) => Behaviors.same
      case PickupCoffee(coffee) => onPickupCoffee(coffee, context)
      case _ => Behaviors.same
    }
  }
end CoffeeMachineActor
