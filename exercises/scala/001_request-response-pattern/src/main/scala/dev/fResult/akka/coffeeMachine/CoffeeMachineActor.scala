package dev.fResult.akka.coffeeMachine

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import dev.fResult.akka.barista.BaristaCommand.CoffeeReady
import dev.fResult.akka.coffeeMachine.CoffeeMachineCommand.{BrewCoffee, PickupCoffee}

import scala.util.{Failure, Try}

object CoffeeMachineActor:
  def apply(): Behavior[CoffeeMachineCommand] = idle()

  private def idle(): Behavior[CoffeeMachineCommand] = Behaviors.setup { context =>
    context.log.info("CoffeeMachine: IDLE")

    Behaviors.receiveMessage {
      case BrewCoffee(coffee, replyTo) => onBrewCoffee(context, BrewCoffee(coffee, replyTo))
      case _ => Behaviors.same
    }
  }

  private def onBrewCoffee(context: ActorContext[CoffeeMachineCommand], command: BrewCoffee): Behavior[CoffeeMachineCommand] = {
    val coffee = command.coffee
    context.log.info(s"CoffeeMachine: Brewing 1 $coffee")

    Try(Thread.sleep(3000)) match {
      case Failure(ex: InterruptedException) => ex.printStackTrace()
      case _ =>
    }

    command.replyTo ! CoffeeReady(coffee)

    context.log.info("CoffeeMachine: Coffee {} is ready", coffee)

    Behaviors.receiveMessage {
      case PickupCoffee(_) => context.log.info(s"CoffeeMachine: Coffee $coffee is picked up"); idle()
      case BrewCoffee(c, _) => onBrewCoffee(context, command)
    }
  }
end CoffeeMachineActor
