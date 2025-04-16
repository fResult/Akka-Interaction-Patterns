package dev.fResult.akka

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorSystem, Behavior}

import scala.annotation.tailrec

@main
def main(): Unit = {
  val baristaActor = ActorSystem[BaristaCommand](BaristaActor(), "barista")

  baristaActor ! OrderCoffee("Wick", Akkacino)
  baristaActor ! OrderCoffee("Anderson", CaffeJava)
  baristaActor ! OrderCoffee("Constantine", MochaPlay)
}


object BaristaActor {
  private final case class State(orders: Map[String, Coffee] = Map.empty)

  def apply(): Behavior[BaristaCommand] = Behaviors.setup(context => {
    context.log.info("Barista actor is applied")

    val initialState = State()

    Behaviors.receiveMessage {
      case cmd@OrderCoffee(_, _) => onOrderCoffee(cmd, context, initialState)
    }
  })

  private def onOrderCoffee(command: OrderCoffee, context: ActorContext[BaristaCommand], state: State): Behavior[BaristaCommand] = {
    val updatedState = state.copy(orders = state.orders + (command.whom -> command.coffee))

    context.log.info(s"Orders: ${printOrder(updatedState.orders.toSet)}")

    //    onOrderCoffee(command, context, updatedState)
    Behaviors.same
  }

  private def printOrder(orderSet: Set[(String, Coffee)]): String = {
    val formattedOrders = orderSet.map(order => s"${order._1}->${order._2}")

    s"[${formattedOrders.mkString(", ")}]"
  }
}

// <- Barista protocol
sealed trait BaristaCommand

case class OrderCoffee(whom: String, coffee: Coffee) extends BaristaCommand
// Barista protocol ->

// <- Coffee types
sealed trait Coffee

case object Akkacino extends Coffee

case object CaffeJava extends Coffee

case object MochaPlay extends Coffee
// Coffee types ->

