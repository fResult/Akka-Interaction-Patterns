package dev.fResult.akka.coffeeMachine

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import dev.fResult.akka.Coffee
import dev.fResult.akka.barista.BaristaCommand.CoffeeReady
import dev.fResult.akka.coffeeMachine.CoffeeMachineCommand.{BrewCoffee, PickupCoffee}

import scala.util.{Failure, Try}

object CoffeeMachineActor {
  def apply(): Behavior[CoffeeMachineCommand] = idle()

  private def idle(): Behavior[CoffeeMachineCommand] = Behaviors.setup { context =>
    context.log.info("CoffeeMachine: IDLE")

    Behaviors.receiveMessage {
      case command@BrewCoffee(_, _) => onBrewCoffee(command, context)
      case PickupCoffee => Behaviors.same
    }
  }

  private def onBrewCoffee(command: BrewCoffee,
                           context: ActorContext[CoffeeMachineCommand],
                          ): Behavior[CoffeeMachineCommand] =

    val coffee = command.coffee;
    context.log.info(s"CoffeeMachine: Brewing 1 $coffee")

    Try(Thread.sleep(3000)) match
      case Failure(ex: InterruptedException) => ex.printStackTrace()
      case _ =>

    command.replyTo ! CoffeeReady(coffee)

    context.log.info("CoffeeMachine: Coffee {} is ready", coffee)
    Behaviors.receiveMessage {
      case PickupCoffee => onPickupCoffee(context, coffee)
      case command@BrewCoffee(_, _) => onBrewCoffee(command, context)
    }

  private def onPickupCoffee(context: ActorContext[CoffeeMachineCommand], coffee: Coffee) =
    // TODO: Update following the exercise 2, using MessageAdapter
    context.log.info(s"CoffeeCommand: Picking up $coffee")
    idle()
}
