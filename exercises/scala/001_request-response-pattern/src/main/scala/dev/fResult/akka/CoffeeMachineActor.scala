package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import dev.fResult.akka.Coffee
import CoffeeMachineCommand.BrewCoffee

import scala.util.{Failure, Try}

object CoffeeMachineActor:
  def apply(): Behavior[CoffeeMachineCommand] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case BrewCoffee(coffee) => onBrewCoffee(context, coffee)
      case _ => Behaviors.same
    }
  }

  private def onBrewCoffee(context: ActorContext[CoffeeMachineCommand], coffee: Coffee): Behavior[CoffeeMachineCommand] = {
    context.log.info(s"CoffeeMachine: Brewing 1 $coffee")

    Try(Thread.sleep(3000)) match {
      case Failure(ex: InterruptedException) => ex.printStackTrace()
      case _ =>
    }

    // Todo: handle serving coffee
    Behaviors.same
  }
end CoffeeMachineActor
