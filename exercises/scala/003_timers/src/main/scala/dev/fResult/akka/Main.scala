package dev.fResult.akka

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import java.io.IOException
import scala.io.StdIn.readLine
import scala.util.control.Exception
import scala.util.{Failure, Try}

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

    handleCommands(context, coffeeMachineActorRef, initialState)
  })

  private def handleCommands(context: ActorContext[BaristaCommand], coffeeMachineActorRef: ActorRef[CoffeeMachineCommand], state: State): Behavior[BaristaCommand] = {
    Behaviors.receiveMessage {
      case cmd@OrderCoffee(_, _) => onOrderCoffee(cmd, context, coffeeMachineActorRef, state)
      case CoffeeReady(coffee) => {
        coffeeMachineActorRef ! PickupCoffee(coffee)

        handleCommands(context, coffeeMachineActorRef, state)
      }
    }
  }

  private def onOrderCoffee(command: OrderCoffee,
                            context: ActorContext[BaristaCommand],
                            coffeeMachineActor: ActorRef[CoffeeMachineCommand],
                            state: State): Behavior[BaristaCommand] = {

    val updatedState = state.copy(state.orders + (command.whom -> command.coffee))

    context.log.info(s"Orders: ${printOrder(updatedState.orders.toSet)}")

    coffeeMachineActor ! BrewCoffee(command.coffee, context.self)

    Behaviors.receiveMessage {
      case cmd@OrderCoffee(_, _) => onOrderCoffee(cmd, context, coffeeMachineActor, updatedState)
      case CoffeeReady(coffee) => // TODO: Extract to `onCoffeeReady` and remove the order that already finish brewing
        context.log.info(s"Barista: Coffee $coffee is ready...")
        coffeeMachineActor ! PickupCoffee(coffee)
        Behaviors.same
    }
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
}

// <- Barista protocol
sealed trait BaristaCommand

case class OrderCoffee(whom: String, coffee: Coffee) extends BaristaCommand

case class CoffeeReady(coffee: Coffee) extends BaristaCommand
// Barista protocol ->

object CoffeeMachineActor:
  def apply(): Behavior[CoffeeMachineCommand] = idle()

  private def idle(): Behavior[CoffeeMachineCommand] = Behaviors.setup { context =>
    context.log.info("CoffeeMachine: IDLE")

    Behaviors.receiveMessage {
      case cmd@BrewCoffee(_, _) => onBrewCoffee(cmd, context)
      case PickupCoffee(coffee) =>
        context.log.info(s"CoffeeMachine: Picking up coffee: $coffee")
        Behaviors.same
    }
  }

  private def onBrewCoffee(command: BrewCoffee,
                           context: ActorContext[CoffeeMachineCommand],
                          ): Behavior[CoffeeMachineCommand] = {

    context.log.info(s"CoffeeMachine: {} is brewing", command.coffee)

    Try(Thread.sleep(3000)) match
      case Failure(ex: InterruptedException) => ex.printStackTrace()
      case _ =>

    command.replyTo ! CoffeeReady(command.coffee)

    // coffeeReady
    Behaviors.same
  }
end CoffeeMachineActor

// <- CoffeeMachine protocol
sealed trait CoffeeMachineCommand

case class BrewCoffee(coffee: Coffee, replyTo: ActorRef[BaristaCommand]) extends CoffeeMachineCommand

case class PickupCoffee(coffee: Coffee) extends CoffeeMachineCommand
// CoffeeMachine protocol ->


// <- Coffee types
sealed trait Coffee

case object Akkacino extends Coffee

case object CaffeJava extends Coffee

case object MochaPlay extends Coffee
// Coffee types ->

