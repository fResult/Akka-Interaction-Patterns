package dev.fResult.akka.coffeeMachine

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import dev.fResult.akka.Coffee
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

    coffeeReady(context, command)
  }

  private def coffeeReady(context: ActorContext[CoffeeMachineCommand], command: BrewCoffee): Behavior[CoffeeMachineCommand] = {
    val coffee = command.coffee
    context.log.info("CoffeeMachine: Coffee {} is ready", coffee)

    Behaviors.setup(context => Behaviors.receiveMessage {
      case BrewCoffee(c, _) => onBrewCoffee(context, command)
      case PickupCoffee => onPickupCoffee(context, coffee)
    })
  }

  private def onPickupCoffee(context: ActorContext[CoffeeMachineCommand], coffee: Coffee): Behavior[CoffeeMachineCommand] = {
    context.log.info(s"CoffeeMachine: Coffee $coffee is picked up")
    idle()
  }
end CoffeeMachineActor
