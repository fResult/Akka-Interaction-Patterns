package dev.fResult.akka.coffeeCommand

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import dev.fResult.akka.Coffee
import dev.fResult.akka.barista.BaristaCommand.CoffeeReady
import dev.fResult.akka.coffeeCommand.CoffeeMachineCommand.{BrewCoffee, CoffeeReadyTick, PickupCoffee}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

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
