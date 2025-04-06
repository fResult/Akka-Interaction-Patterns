package dev.fResult.akka.coffeeMachine

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import dev.fResult.akka.coffeeMachine.CoffeeMachineCommand.BrewCoffee

object CoffeeMachineActor {
  def apply(): Behavior[CoffeeMachineCommand] = Behaviors.setup { context =>
    context.log.info("CoffeeMachine: IDLE")

    Behaviors.receiveMessage {
      case BrewCoffee(coffee, _) => Behaviors.same
      case _ => Behaviors.same
    }
  }
}
