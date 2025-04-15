package com.akka.training.coffeeMachine;

import akka.actor.typed.ActorRef;
import com.akka.training.Coffee;

public sealed interface CoffeeMachineCommand
    permits CoffeeMachineCommand.BrewCoffee,
        CoffeeMachineCommand.CoffeeReady,
        CoffeeMachineCommand.PickupCoffee {

  record BrewCoffee(Coffee coffee, ActorRef<CoffeeReady> replyTo) implements CoffeeMachineCommand {}

  record CoffeeReady(Coffee coffee) implements CoffeeMachineCommand {}

  /*
   * Represents the Barista picking up the coffee and resetting the coffee machine, so that it's ready for the next coffee
   */
  static final class PickupCoffee implements CoffeeMachineCommand {}
}
