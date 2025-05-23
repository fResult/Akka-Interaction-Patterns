package dev.fResult.akka.coffeeCommand

import akka.actor.typed.ActorRef
import dev.fResult.akka.Coffee
import dev.fResult.akka.barista.BaristaCommand

enum CoffeeMachineCommand {
  case BrewCoffee(coffee: Coffee, replyTo: ActorRef[BaristaCommand])
  case PickupCoffee(coffee: Coffee)
  case CoffeeReadyTick
}
