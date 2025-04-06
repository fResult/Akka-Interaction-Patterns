package dev.fResult.akka

import dev.fResult.akka.Coffee

enum CoffeeMachineCommand {
  case BrewCoffee(coffee: Coffee)
  case PickupCoffee(coffee: Coffee)
}
