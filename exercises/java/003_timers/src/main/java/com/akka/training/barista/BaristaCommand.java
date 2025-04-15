package com.akka.training.barista;

import com.akka.training.Coffee;
import com.akka.training.coffeeMachine.CoffeeMachineCommand;

public sealed interface BaristaCommand
    permits BaristaCommand.OrderCoffee, BaristaCommand.WrappedCoffeeMachineCoffeeReady {

  record OrderCoffee(String whom, Coffee coffee) implements BaristaCommand {}

  record WrappedCoffeeMachineCoffeeReady(CoffeeMachineCommand.CoffeeReady coffeeReady)
      implements BaristaCommand {}
}
