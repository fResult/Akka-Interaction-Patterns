package dev.fResult.akka.coffeeMachine

import dev.fResult.akka.Coffee

enum CoffeeMachineCommand {
  case BrewCoffee(coffee: Coffee)
  case PickupCoffee(coffee: Coffee)
}
